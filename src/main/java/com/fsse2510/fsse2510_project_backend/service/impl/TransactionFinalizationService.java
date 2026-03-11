package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.request.CartItemRequestData;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response.TransactionResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.exception.transaction.TransactionNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.transaction.TransactionDataMapper;
import com.fsse2510.fsse2510_project_backend.repository.TransactionRepository;

import com.fsse2510.fsse2510_project_backend.service.CartItemService;
import com.fsse2510.fsse2510_project_backend.service.CouponService;
import com.fsse2510.fsse2510_project_backend.service.MembershipService;
import com.fsse2510.fsse2510_project_backend.service.ProductAdminService;
import com.fsse2510.fsse2510_project_backend.service.StripeService;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Handles the second phase of the split-transaction flow:
 * stock deduction, points/membership finalization, failure recovery, and cart
 * restoration.
 * Extracted from TransactionServiceImpl to break the self-proxy circular
 * dependency.
 */
@Service
@RequiredArgsConstructor
public class TransactionFinalizationService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionFinalizationService.class);
    private static final String PAYMENT_READY_SUCCESS = "succeeded";
    private static final String PAYMENT_READY_CAPTURE = "requires_capture";

    private final TransactionRepository transactionRepository;
    private final TransactionDataMapper transactionDataMapper;
    private final UserService userService;

    private final ProductAdminService productAdminService;
    private final MembershipService membershipService;
    private final CouponService couponService;
    private final StripeService stripeService;
    private final CartItemService cartItemService;

    @Transactional
    public TransactionResponseData finalizeSuccess(FirebaseUserData firebaseUser, Integer tid,
            MembershipLevel prefLevel) {
        TransactionEntity transaction = getOwnedTransactionWithLock(firebaseUser, tid);

        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return transactionDataMapper.toData(transaction);
        }

        deductStock(transaction);
        finalizeUserPointsAndMembership(transaction);

        transaction.setStatus(PaymentStatus.SUCCESS);
        TransactionEntity savedTx = transactionRepository.save(transaction);

        TransactionResponseData response = transactionDataMapper.toData(savedTx);
        response.setPreviousLevel(prefLevel);
        response.setNewLevel(savedTx.getUser().getLevel());
        return response;
    }

    @Transactional
    public TransactionResponseData adminFinalizeSuccess(Integer tid) {
        TransactionEntity transaction = transactionRepository.findById(tid)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + tid));

        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return transactionDataMapper.toData(transaction);
        }

        deductStock(transaction);
        finalizeUserPointsAndMembership(transaction);

        transaction.setStatus(PaymentStatus.SUCCESS);
        TransactionEntity savedTx = transactionRepository.save(transaction);

        return transactionDataMapper.toData(savedTx);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailureWithRecovery(FirebaseUserData firebaseUser, Integer tid,
            PaymentIntent intent, Exception e) {
        TransactionEntity transaction = getOwnedTransaction(firebaseUser, tid);
        handleFinalizationFailure(transaction, intent, e);
    }

    @Transactional
    public void recoverCartItems(TransactionEntity transaction) {
        try {
            logger.info("Recovering cart items for User={} from Transaction={}",
                    transaction.getUser().getUid(), transaction.getTid());

            FirebaseUserData firebaseUser = new FirebaseUserData();
            firebaseUser.setFirebaseUid(transaction.getUser().getFirebaseUid());

            for (TransactionProductEntity item : transaction.getItems()) {
                CartItemRequestData recoverReq = new CartItemRequestData();
                recoverReq.setSku(item.getSku());
                recoverReq.setQuantity(item.getQuantity());
                recoverReq.setUser(firebaseUser);
                cartItemService.addCartItem(recoverReq);
            }
            logger.info("Cart recovery complete for TID={}", transaction.getTid());
        } catch (Exception e) {
            logger.error("Cart recovery failed for TID={}: {}", transaction.getTid(), e.getMessage());
        }
    }

    private void deductStock(TransactionEntity transaction) {
        java.util.Map<String, Integer> skuQuantityMap = new java.util.HashMap<>();
        for (TransactionProductEntity item : transaction.getItems()) {
            skuQuantityMap.put(item.getSku(), skuQuantityMap.getOrDefault(item.getSku(), 0) + item.getQuantity());
        }
        productAdminService.deductStock(skuQuantityMap);
    }

    private void finalizeUserPointsAndMembership(TransactionEntity transaction) {
        UserEntity user = userService.findEntityByIdWithLock(transaction.getUser().getUid());

        // 1. Deduct used points first (if any)
        if (transaction.getUsedPoints() > 0) {
            user.setPoints(user.getPoints().subtract(BigDecimal.valueOf(transaction.getUsedPoints())));
        }

        // 2. Perform membership renewal/upgrade check (might reset points balance)
        membershipService.checkStatusAndAutoUpdate(user);
        membershipService.accumulateAndCheckUpgrade(user, transaction.getTotal());

        // 3. Add earnings from this transaction AFTER any potential renewal reset
        BigDecimal earned = membershipService.calculateEarnedPoints(user, transaction.getTotal());
        transaction.setEarnedPoints(earned);
        user.setPoints(user.getPoints().add(earned));

        if (transaction.getCouponCode() != null) {
            couponService.incrementUsage(transaction.getCouponCode());
        }

        userService.saveUser(user);
    }

    private void handleFinalizationFailure(TransactionEntity transaction, PaymentIntent intent, Exception e) {
        logger.error("Error finalizing transaction {}. Error: {}", transaction.getTid(), e.getMessage());

        if (intent != null && PAYMENT_READY_SUCCESS.equals(intent.getStatus())) {
            // Payment already captured — money is taken. Keep PROCESSING so Stripe
            // webhook retry can re-attempt finalization. Do NOT mark FAILED or recover
            // cart.
            logger.error("FATAL INTEGRITY ALERT: TID {} failed finalization BUT PAYMENT IS ALREADY CAPTURED. " +
                    "This indicates a potential overselling scenario in the 30-min window. " +
                    "Action Required: Manual stock adjustment or refund. Error: {}",
                    transaction.getTid(), e.getMessage());
            return;
        }

        if (intent != null && PAYMENT_READY_CAPTURE.equals(intent.getStatus())) {
            try {
                stripeService.cancelPayment(intent, transaction.getTid());
                logger.info("Successfully released Stripe authorization for TID: {}", transaction.getTid());
            } catch (Exception se) {
                logger.error("Failed to release Stripe authorization for TID: {}. Error: {}",
                        transaction.getTid(), se.getMessage());
            }
        }

        transaction.setStatus(PaymentStatus.FAILED);
        transactionRepository.save(transaction);
        recoverCartItems(transaction);
    }

    private TransactionEntity getOwnedTransaction(FirebaseUserData firebaseUser, Integer tid) {
        UserData userData = userService.getOrCreateUser(firebaseUser);
        return transactionRepository.findByTidAndUser_Uid(tid, userData.getUid())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found or access denied"));
    }

    private TransactionEntity getOwnedTransactionWithLock(FirebaseUserData firebaseUser, Integer tid) {
        UserData userData = userService.getOrCreateUser(firebaseUser);
        return transactionRepository.findByTidAndUser_UidWithLock(tid, userData.getUid())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found or access denied"));
    }
}
