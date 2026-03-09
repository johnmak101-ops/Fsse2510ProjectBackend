package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity;
import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.payment.domainObject.response.PaymentResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.request.CreateTransactionRequestData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response.TransactionResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.exception.address.AddressNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.address.MissingShippingAddressException;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.CartEmptyException;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.NotEnoughStockException;
import com.fsse2510.fsse2510_project_backend.exception.transaction.IllegalPaymentOperationException;
import com.fsse2510.fsse2510_project_backend.exception.transaction.PaymentVerificationException;
import com.fsse2510.fsse2510_project_backend.exception.transaction.TransactionIllegalStateException;
import com.fsse2510.fsse2510_project_backend.exception.transaction.TransactionNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.transaction.TransactionDataMapper;
import com.fsse2510.fsse2510_project_backend.repository.ShippingAddressRepository;
import com.fsse2510.fsse2510_project_backend.repository.TransactionRepository;
import com.fsse2510.fsse2510_project_backend.repository.UserRepository;
import com.fsse2510.fsse2510_project_backend.service.CartItemService;
import com.fsse2510.fsse2510_project_backend.service.CouponService;
import com.fsse2510.fsse2510_project_backend.service.StripeService;
import com.fsse2510.fsse2510_project_backend.service.TransactionService;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import com.fsse2510.fsse2510_project_backend.util.BusinessConstants;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private static final String BYPASS_SESSION_ID = BusinessConstants.STRIPE_BYPASS_SESSION_ID;
    private static final String PAYMENT_READY_SUCCESS = "succeeded";
    private static final String PAYMENT_READY_CAPTURE = "requires_capture";
    private static final int POINTS_TO_DOLLAR_RATE = BusinessConstants.POINTS_TO_DOLLAR_RATE;
    private static final int MONEY_SCALE = BusinessConstants.MONEY_SCALE;
    private static final List<PaymentStatus> ACTIVE_PENDING_STATUSES = List.of(PaymentStatus.PENDING,
            PaymentStatus.PROCESSING);

    private final UserService userService;
    private final CartItemService cartItemService;
    private final StripeService stripeService;
    private final CouponService couponService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final TransactionDataMapper transactionDataMapper;
    private final TransactionFinalizationService finalizationService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public TransactionResponseData createTransaction(CreateTransactionRequestData requestData) {
        logger.info("Creating transaction for user: {}", requestData.getUser().getFirebaseUid());

        UserData userData = userService.getOrCreateUser(requestData.getUser());
        List<CartItemResponseData> cartItems = fetchAndValidateCart(requestData.getUser());

        TransactionEntity transaction = initTransactionEntity(userData);
        BigDecimal totalAmount = processTransactionItems(transaction, cartItems);

        totalAmount = applyCouponIfPresent(transaction, requestData.getCouponCode(), totalAmount,
                userData.getMembership().getLevel());
        totalAmount = applyPointsIfRequested(transaction, userData, requestData.getUsePoints(), totalAmount);

        snapshotAddress(transaction, userData.getUid(), requestData.getAddressId());

        BigDecimal finalTotal = totalAmount.max(BigDecimal.ZERO);
        validateNotInPaymentDeadZone(finalTotal);
        transaction.setTotal(finalTotal);
        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        clearUserCart(requestData.getUser(), cartItems);

        return transactionDataMapper.toData(savedTransaction);
    }

    @Override
    @Transactional
    public PaymentResponseData preparePayment(FirebaseUserData firebaseUser, Integer tid) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUser, tid);

        if (transaction.getStatus() == PaymentStatus.ABORTED || transaction.getStatus() == PaymentStatus.FAILED) {
            throw new TransactionIllegalStateException(
                    "Cannot pay for a " + transaction.getStatus().name().toLowerCase()
                            + " transaction. Please create a new order.");
        }

        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return PaymentResponseData.builder().clientSecret("ALREADY_PAID")
                    .transactionId(transaction.getTid()).amount(transaction.getTotal()).build();
        }

        if (transaction.getTotal().compareTo(BigDecimal.ZERO) == 0) {
            String baseUrl = getBaseUrl();
            return PaymentResponseData.builder().transactionId(transaction.getTid()).amount(BigDecimal.ZERO)
                    .url(baseUrl + "/checkout/success?tid=" + tid + "&session_id=skip_stripe").build();
        }

        validateMinimumAmount(transaction.getTotal());

        String baseUrl = getBaseUrl();
        String successUrl = baseUrl + "/checkout/success?tid=" + tid + "&session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = baseUrl + "/checkout/cancel?tid=" + tid;

        Session session = stripeService.createCheckoutSession(tid, firebaseUser, transaction.getTotal(), successUrl,
                cancelUrl);
        return PaymentResponseData.builder().url(session.getUrl()).transactionId(transaction.getTid())
                .amount(transaction.getTotal()).build();
    }

    @Override
    public TransactionResponseData finishTransaction(FirebaseUserData firebaseUser, Integer tid, String sessionId) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUser, tid);

        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return transactionDataMapper.toData(transaction);
        }

        if (transaction.getStatus() == PaymentStatus.PENDING) {
            transaction.setStatus(PaymentStatus.PROCESSING);
            transaction = transactionRepository.save(transaction);
        }

        PaymentIntent intent = null;
        if (!isBypassStripe(transaction, sessionId)) {
            intent = recoverAndVerifyPaymentIntent(firebaseUser, transaction, sessionId);
        }

        try {
            if (intent != null && PAYMENT_READY_CAPTURE.equals(intent.getStatus())) {
                stripeService.capturePayment(intent, transaction.getTid());
            }

            MembershipLevel prefLevel = transaction.getUser().getLevel();
            return finalizationService.finalizeSuccess(firebaseUser, tid, prefLevel);

        } catch (RuntimeException e) {
            finalizationService.handleFailureWithRecovery(firebaseUser, tid, intent, e);
            throw e;
        } catch (Exception e) {
            finalizationService.handleFailureWithRecovery(firebaseUser, tid, intent, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TransactionResponseData> getTransactions(FirebaseUserData firebaseUser) {
        UserData userData = userService.getOrCreateUser(firebaseUser);
        return transactionRepository.findAllByUser_Uid(userData.getUid()).stream()
                .map(transactionDataMapper::toData).toList();
    }

    @Override
    public TransactionResponseData getTransactionById(FirebaseUserData firebaseUser, Integer tid) {
        return transactionDataMapper.toData(getOwnedTransaction(firebaseUser, tid));
    }

    @Override
    @Transactional
    public TransactionResponseData abortTransaction(FirebaseUserData firebaseUser, Integer tid) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUser, tid);
        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            throw new TransactionIllegalStateException("Cannot abort successful transaction");
        }
        transaction.setStatus(PaymentStatus.ABORTED);
        return saveAndRecover(transaction);
    }

    @Override
    @Transactional
    public TransactionResponseData updateStatusToProcessing(FirebaseUserData firebaseUser, Integer tid) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUser, tid);
        if (transaction.getStatus() == PaymentStatus.PENDING) {
            transaction.setStatus(PaymentStatus.PROCESSING);
            return transactionDataMapper.toData(transactionRepository.save(transaction));
        }
        return transactionDataMapper.toData(transaction);
    }

    @Override
    @Transactional
    public void finishTransactionFromStripeWebhook(String firebaseUid, Integer tid, String intentId,
                                                   Long amount, String currency) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUid, tid);
        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }
        if (transaction.getStripePaymentIntentId() == null
                || !transaction.getStripePaymentIntentId().equals(intentId)) {
            throw new PaymentVerificationException("Payment Intent Mismatch");
        }
        finishTransaction(new FirebaseUserData(firebaseUid, null), tid, null);
    }

    @Override
    @Transactional
    public void finishTransactionFromStripeCheckout(String firebaseUid, Integer tid,
                                                    String intentId) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUid, tid);
        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }
        transaction.setStripePaymentIntentId(intentId);
        transactionRepository.save(transaction);
        finishTransaction(new FirebaseUserData(firebaseUid, null), tid, null);
    }

    @Override
    @Transactional
    public void failTransactionFromStripeWebhook(String firebaseUid, Integer tid, String intentId) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUid, tid);
        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }
        if (transaction.getStripePaymentIntentId() == null
                || !transaction.getStripePaymentIntentId().equals(intentId)) {
            throw new PaymentVerificationException("Payment Intent Mismatch");
        }
        transaction.setStatus(PaymentStatus.FAILED);
        saveAndRecover(transaction);
    }

    @Override
    @Transactional
    public void abortTransactionFromStripeWebhook(String firebaseUid, Integer tid) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUid, tid);

        if (transaction.getStatus() == PaymentStatus.SUCCESS
                || transaction.getStatus() == PaymentStatus.ABORTED) {
            logger.info("[Webhook] Transaction {} already in final state: {}, skipping abort",
                    tid, transaction.getStatus());
            return;
        }

        logger.info("[Webhook] Aborting transaction {} due to payment_intent.canceled (user abandoned)", tid);
        transaction.setStatus(PaymentStatus.ABORTED);
        saveAndRecover(transaction);
    }

    @Override
    public List<TransactionResponseData> getAllTransactions() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "datetime"))
                .stream().map(transactionDataMapper::toData).toList();
    }

    @Override
    public TransactionResponseData getAdminTransactionById(Integer tid) {
        return transactionDataMapper.toData(transactionRepository.findById(tid)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + tid)));
    }

    private TransactionEntity getOwnedTransaction(FirebaseUserData firebaseUser, Integer tid) {
        UserData userData = userService.getOrCreateUser(firebaseUser);
        return transactionRepository.findByTidAndUser_Uid(tid, userData.getUid())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found or access denied"));
    }

    private String getBaseUrl() {
        if (frontendUrl == null)
            return "http://localhost:3000";
        // If multiple URLs exist (for CORS), take the first one as the redirect base
        return frontendUrl.split(",")[0].trim();
    }

    private TransactionEntity getOwnedTransaction(String firebaseUid, Integer tid) {
        return getOwnedTransaction(new FirebaseUserData(firebaseUid, null), tid);
    }

    private boolean isBypassStripe(TransactionEntity transaction, String sessionId) {
        boolean isBypassId = BYPASS_SESSION_ID.equals(sessionId);
        boolean isZeroAmount = transaction.getTotal().compareTo(BigDecimal.ZERO) == 0;

        if (isBypassId && !isZeroAmount) {
            logger.error("Security Alert: Attempted to bypass Stripe for non-zero amount. TID={}, User={}",
                    transaction.getTid(), transaction.getUser().getUid());
            throw new IllegalPaymentOperationException("Bypass only allowed for free items");
        }

        return isBypassId || isZeroAmount;
    }

    private PaymentIntent recoverAndVerifyPaymentIntent(FirebaseUserData firebaseUser, TransactionEntity transaction,
            String sessionId) {
        if (transaction.getStripePaymentIntentId() == null && sessionId != null) {
            Session session = stripeService.retrieveSession(sessionId);
            validateSession(session, transaction, firebaseUser);

            if (session.getPaymentIntent() != null) {
                transaction.setStripePaymentIntentId(session.getPaymentIntent());
                transactionRepository.save(transaction);
            }
        }

        if (transaction.getStripePaymentIntentId() == null) {
            throw new PaymentVerificationException("No Payment Intent found for transaction: " + transaction.getTid());
        }

        PaymentIntent intent = stripeService.retrievePaymentIntent(transaction.getStripePaymentIntentId());
        if (!PAYMENT_READY_SUCCESS.equals(intent.getStatus()) && !PAYMENT_READY_CAPTURE.equals(intent.getStatus())) {
            throw new PaymentVerificationException("Payment not ready. Status: " + intent.getStatus());
        }
        return intent;
    }

    private void validateSession(Session session, TransactionEntity transaction, FirebaseUserData firebaseUser) {
        if (!session.getClientReferenceId().equals(transaction.getTid().toString())) {
            throw new IllegalPaymentOperationException("Invalid Session ID");
        }
        String sessionUid = session.getMetadata().get("uid");
        if (sessionUid != null && !sessionUid.equals(firebaseUser.getFirebaseUid())) {
            throw new IllegalPaymentOperationException("Invalid Session User");
        }
    }

    private void validateMinimumAmount(BigDecimal total) {
        if (total.compareTo(BusinessConstants.MIN_PAYMENT_AMOUNT) < 0) {
            throw new IllegalPaymentOperationException("Transaction amount too low for payment provider");
        }
    }

    private void validateNotInPaymentDeadZone(BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) > 0
                && total.compareTo(BusinessConstants.MIN_PAYMENT_AMOUNT) < 0) {
            throw new IllegalPaymentOperationException(
                    "Order total HKD " + total.toPlainString()
                            + " falls below minimum payment amount of HKD "
                            + BusinessConstants.MIN_PAYMENT_AMOUNT.toPlainString()
                            + ". Please adjust points usage to fully cover the order or reduce points to keep total above minimum.");
        }
    }

    private List<CartItemResponseData> fetchAndValidateCart(FirebaseUserData user) {
        List<CartItemResponseData> cartItems = cartItemService.getUserCart(user);
        if (cartItems.isEmpty()) {
            throw new CartEmptyException("Cart is empty");
        }
        return cartItems;
    }

    private TransactionEntity initTransactionEntity(UserData userData) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setUser(userRepository.getReferenceById(userData.getUid()));
        transaction.setDatetime(LocalDateTime.now());
        transaction.setStatus(PaymentStatus.PENDING);
        return transaction;
    }

    private BigDecimal processTransactionItems(TransactionEntity transaction, List<CartItemResponseData> cartItems) {
        BigDecimal total = BigDecimal.ZERO;
        List<TransactionProductEntity> items = new ArrayList<>();
        for (CartItemResponseData item : cartItems) {
            long virtualStock = getVirtualStock(item.getSku(), item.getStock());
            if (item.getCartQuantity() > virtualStock) {
                logger.warn(
                        "Pre-Payment Stock Check Failed (Virtual): Item {} (SKU: {}) requested {}, but virtual stock is {}.",
                        item.getName(), item.getSku(), item.getCartQuantity(), virtualStock);
                throw new NotEnoughStockException("Not enough stock for item: " + item.getName());
            }

            BigDecimal subtotal = scaleAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getCartQuantity())));
            total = total.add(subtotal);

            TransactionProductEntity tp = TransactionProductEntity.builder()
                    .transaction(transaction)
                    .pid(item.getPid())
                    .sku(item.getSku())
                    .name(item.getName())
                    .imageUrl(item.getImageUrl())
                    .price(item.getPrice())
                    .quantity(item.getCartQuantity())
                    .subtotal(subtotal)
                    .size(item.getSelectedSize())
                    .color(item.getSelectedColor())
                    .build();
            items.add(tp);
        }
        transaction.setItems(items);
        return total;
    }

    private BigDecimal applyCouponIfPresent(TransactionEntity transaction, String couponCode, BigDecimal currentTotal,
            MembershipLevel userLevel) {
        if (couponCode == null || couponCode.isBlank()) {
            return currentTotal;
        }

        CouponResponseData coupon = couponService.validateCoupon(couponCode, currentTotal, userLevel);

        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = scaleAmount(currentTotal.multiply(
                    coupon.getDiscountValue().divide(BigDecimal.valueOf(100), MONEY_SCALE, RoundingMode.HALF_UP)));
        } else {
            discount = scaleAmount(coupon.getDiscountValue());
        }

        transaction.setCouponCode(couponCode);
        return scaleAmount(currentTotal.subtract(discount).max(BigDecimal.ZERO));
    }

    private BigDecimal applyPointsIfRequested(TransactionEntity transaction, UserData userData, Integer usePoints,
            BigDecimal currentTotal) {
        if (usePoints == null || usePoints <= 0 || userData.getPoints() == null) {
            return currentTotal;
        }

        BigDecimal virtualPoints = getVirtualPoints(userData.getUid(), userData.getPoints());
        if (virtualPoints.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Virtual Points Check Failed: User {} has 0 virtual points due to pending transactions.",
                    userData.getUid());
            return currentTotal;
        }

        BigDecimal pointsRequested = BigDecimal.valueOf(usePoints).min(virtualPoints);
        BigDecimal actualDiscount = calculatePointsDiscount(pointsRequested, virtualPoints, currentTotal);
        int pointsActuallyUsed = actualDiscount.multiply(BigDecimal.valueOf(POINTS_TO_DOLLAR_RATE))
                .setScale(0, RoundingMode.CEILING).intValue();

        transaction.setUsedPoints(pointsActuallyUsed);
        return scaleAmount(currentTotal.subtract(actualDiscount));
    }

    private BigDecimal calculatePointsDiscount(BigDecimal pointsRequested, BigDecimal virtualPoints,
            BigDecimal currentTotal) {
        BigDecimal discountValue = pointsRequested.divide(
                BigDecimal.valueOf(POINTS_TO_DOLLAR_RATE), MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal maxDiscount = virtualPoints.divide(
                BigDecimal.valueOf(POINTS_TO_DOLLAR_RATE), MONEY_SCALE, RoundingMode.HALF_UP).min(currentTotal);
        return scaleAmount(discountValue.min(maxDiscount));
    }

    // Helper Method

    private TransactionResponseData saveAndRecover(TransactionEntity transaction) {
        TransactionEntity savedTx = transactionRepository.save(transaction);
        finalizationService.recoverCartItems(savedTx);
        return transactionDataMapper.toData(savedTx);
    }

    private void clearUserCart(FirebaseUserData user, List<CartItemResponseData> items) {
        UserData userData = userService.getOrCreateUser(user);
        UserEntity userRef = userRepository.getReferenceById(userData.getUid());
        cartItemService.clearCart(userRef);
        logger.info("Cart cleared for User={} ({} items removed)", userData.getUid(), items.size());
    }

    private void snapshotAddress(TransactionEntity transaction, Integer uid, Integer addressId) {
        if (addressId == null) {
            throw new MissingShippingAddressException("Shipping address is required for checkout");
        }

        ShippingAddressEntity address = shippingAddressRepository.findById(addressId)
                .filter(a -> a.getUser().getUid().equals(uid))
                .orElseThrow(() -> new AddressNotFoundException("Shipping address not found or access denied"));

        transaction.setRecipientName(address.getRecipientName());
        transaction.setPhoneNumber(address.getPhoneNumber());
        transaction.setAddressLine1(address.getAddressLine1());
        transaction.setAddressLine2(address.getAddressLine2());
        transaction.setCity(address.getCity());
        transaction.setStateProvince(address.getStateProvince());
        transaction.setPostalCode(address.getPostalCode());
    }

    private long getVirtualStock(String sku, int currentStock) {
        Long pendingQty = transactionRepository.sumPendingQuantityBySku(sku, ACTIVE_PENDING_STATUSES);
        long pending = (pendingQty != null) ? pendingQty : 0L;
        return Math.max(0, currentStock - pending);
    }

    private BigDecimal getVirtualPoints(Integer uid, BigDecimal currentPoints) {
        Long pendingPointsLong = transactionRepository.sumPendingPointsByUser(uid, ACTIVE_PENDING_STATUSES);
        BigDecimal pending = (pendingPointsLong != null) ? BigDecimal.valueOf(pendingPointsLong) : BigDecimal.ZERO;
        return currentPoints.subtract(pending).max(BigDecimal.ZERO);
    }

    private BigDecimal scaleAmount(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}