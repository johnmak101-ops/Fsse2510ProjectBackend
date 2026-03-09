package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.coupon.domainObject.response.CouponResponseData;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.payment.domainObject.response.PaymentResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.request.CreateTransactionRequestData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response.TransactionResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.transactionProduct.entity.TransactionProductEntity;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.mapper.transaction.TransactionDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.user.UserDataMapper;
import com.fsse2510.fsse2510_project_backend.repository.TransactionRepository;
import com.fsse2510.fsse2510_project_backend.repository.UserRepository;
import com.fsse2510.fsse2510_project_backend.service.impl.TransactionServiceImpl;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity;
import com.fsse2510.fsse2510_project_backend.exception.transaction.IllegalPaymentOperationException;
import com.fsse2510.fsse2510_project_backend.repository.ShippingAddressRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;

@ExtendWith(MockitoExtension.class)
class TransactionPointsDiscrepancyTest {

        @Mock
        private TransactionRepository transactionRepository;
        @Mock
        private TransactionDataMapper transactionDataMapper;
        @Mock
        private UserRepository userRepository;
        @Mock
        private UserService userService;
        @Mock
        private CartItemService cartItemService;
        @Mock
        private TransactionProductService transactionProductService;
        @Mock
        private ProductService productService;
        @Mock
        private CouponService couponService;
        @Mock
        private MembershipService membershipService;
        @Mock
        private UserDataMapper userDataMapper;
        @Mock
        private ShippingAddressRepository shippingAddressRepository;

        @InjectMocks
        private TransactionServiceImpl transactionService;

        private MockedStatic<Session> sessionMockedStatic;

        @BeforeEach
        void setUp() {
                // Prepare Stripe setup if needed
        }

        @Test
        void testDiscrepancyReproduction() {
                // --- Setup Data ---
                // 1. User
                FirebaseUserData firebaseUser = new FirebaseUserData("uid", "email");
                UserEntity userEntity = new UserEntity();
                userEntity.setUid(1);
                userEntity.setPoints(new BigDecimal("500")); // User has 500 points

                // 2. Cart Item: Price 34.5, Qty 1
                CartItemResponseData cartItem = CartItemResponseData.builder()
                                .pid(1).cartQuantity(1).price(new BigDecimal("34.50"))
                                .sku("SKU-123")
                                .name("Test Product")
                                .stock(10) // [NEW] Added for Pre-Payment Stock Validation
                                .build();

                // 3. Product Data
                ProductResponseData productData = new ProductResponseData();
                productData.setPid(1);
                productData.setName("Test Product");
                productData.setPrice(new BigDecimal("34.50"));

                // --- Mock Behavior ---
                com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipResponseData memData = new com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipResponseData();
                memData.setLevel(
                                com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel.NO_MEMBERSHIP);

                when(userService.getOrCreateUser(firebaseUser))
                                .thenReturn(UserData.builder().uid(1).points(new BigDecimal("500")).membership(memData)
                                                .build());
                when(userRepository.getReferenceById(1)).thenReturn(userEntity);
                // Ensure getByUser_Uid also returns the entity mock
                // Not used in createTransaction but used in preparePayment via
                // getOwnedTransaction -> findByTidAndUser_Uid

                when(cartItemService.getUserCart(firebaseUser)).thenReturn(List.of(cartItem));

                // Mock Coupon Service: Assume NO Coupon first, just points.
                // Wait, User said: Total 34.5, Points 330, Payable 0.5.
                // 34.5 - 33.0 = 1.5. Where does 0.5 come from? Maybe a coupon of $1?
                // Let's assume there IS a coupon to match the 0.5 payable result.
                // Or maybe the item was 33.5?
                // Let's try matching the 0.5 result mathematically.
                // If 330 points = $33.
                // Then remaining is 0.5.
                // So original total MUST be 33.5.
                // OR original was 34.5 and coupon is $1.

                // Let's assume Coupon of $1 (Fixed)
                CouponResponseData coupon = CouponResponseData.builder()
                                .discountType(DiscountType.FIXED)
                                .discountValue(new BigDecimal("1.00"))
                                .build();

                // Use arg captor for coupon code
                // when(couponService.bestCoupon?? No, user provides code
                // We need to simulate user providing coupon code in request.

                // Let's simulate Coupon Service validation
                // Assume user provides "COUPON1"
                when(couponService.validateCoupon(eq("COUPON1"), any(BigDecimal.class),
                                eq(MembershipLevel.NO_MEMBERSHIP))).thenReturn(coupon);

                // Mock Save
                ArgumentCaptor<TransactionEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
                when(transactionRepository.save(transactionCaptor.capture())).thenAnswer(i -> {
                        TransactionEntity t = i.getArgument(0);
                        t.setTid(100); // Simulate DB ID
                        return t;
                });

                // Mock Mapper
                when(transactionDataMapper.toData(any())).thenAnswer(i -> {
                        TransactionEntity t = i.getArgument(0);
                        return TransactionResponseData.builder()
                                        .tid(t.getTid())
                                        .total(t.getTotal())
                                        .build();
                });

                ShippingAddressEntity addressEntity = new ShippingAddressEntity();
                addressEntity.setUser(userEntity);
                when(shippingAddressRepository.findById(1)).thenReturn(Optional.of(addressEntity));

                // --- Execute CREATE ---
                CreateTransactionRequestData createRequest = CreateTransactionRequestData.builder()
                                .user(firebaseUser)
                                .couponCode("COUPON1")
                                .usePoints(330) // 330 Points = $33.00
                                .addressId(1)
                                .build();

                TransactionResponseData createResult = transactionService.createTransaction(createRequest);

                // --- Verify CREATE Result ---
                // 1. Initial Calculation: 34.50
                // 2. Coupon: 34.50 - 1.00 = 33.50
                // 3. Points: 330 points = 33.00 discount
                // 4. Final: 33.50 - 33.00 = 0.50
                System.out.println("Transaction Total: " + createResult.getTotal());
                assertEquals(0, new BigDecimal("0.50").compareTo(createResult.getTotal()),
                                "Transaction total should be 0.50");

                // --- Prepare Payment (Stripe) ---
                // Now simulate preparePayment and see what gets sent to Stripe

                // Mock finding the transaction
                when(transactionRepository.findByTidAndUser_Uid(eq(100), eq(1))).thenAnswer(i -> {
                        return Optional.of(transactionCaptor.getValue());
                });

                // Mock Stripe Session.create - Removed because we expect ProviderException
                // before calling Stripe

                // Expect ProviderException because 0.50 < 4.00
                IllegalPaymentOperationException exception = Assertions
                                .assertThrows(
                                                IllegalPaymentOperationException.class,
                                                () -> transactionService.preparePayment(firebaseUser, 100));

                assertEquals(
                                "Transaction amount too low for payment provider",
                                exception.getMessage());
        }
}
