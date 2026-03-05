package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.cartitem.domainObject.response.CartItemResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.request.CreateTransactionRequestData;
import com.fsse2510.fsse2510_project_backend.data.transaction.domainObject.response.TransactionResponseData;
import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.mapper.transaction.TransactionDataMapper;
import com.fsse2510.fsse2510_project_backend.repository.TransactionRepository;
import com.fsse2510.fsse2510_project_backend.repository.UserRepository;
import com.fsse2510.fsse2510_project_backend.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

        // Core Dependencies
        @Mock
        private TransactionRepository transactionRepository;
        @Mock
        private TransactionDataMapper transactionDataMapper;
        @Mock
        private UserRepository userRepository;

        // Service Dependencies
        @Mock
        private UserService userService;
        @Mock
        private CartItemService cartItemService;
        @Mock
        private ProductService productService;

        // [Fix] Must include these newly added dependencies
        @Mock
        private CouponService couponService;
        @Mock
        private MembershipService membershipService;
        @Mock
        private com.fsse2510.fsse2510_project_backend.repository.ShippingAddressRepository shippingAddressRepository;

        @InjectMocks
        private TransactionServiceImpl transactionService;

        @Test
        void testCreateTransactionSuccess() {
                // --- Setup Data ---
                FirebaseUserData firebaseUser = new FirebaseUserData("uid", "email");
                UserEntity userEntity = new UserEntity();
                userEntity.setUid(1);
                userEntity.setPoints(BigDecimal.ZERO); // Avoid Points check NPE

                CartItemResponseData cartItem = CartItemResponseData.builder()
                                .pid(1).cartQuantity(2).price(new BigDecimal("100.00"))
                                .name("Test Product")
                                .sku("SKU-123")
                                .stock(10) // [NEW] Added for Pre-Payment Stock Validation
                                .build();

                // --- Define Mock Behavior ---

                // 1. User & Cart
                com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipResponseData memData = new com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipResponseData();
                memData.setLevel(
                                com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel.NO_MEMBERSHIP);
                when(userService.getOrCreateUser(firebaseUser))
                                .thenReturn(UserData.builder().uid(1).membership(memData).build());
                when(cartItemService.getUserCart(firebaseUser)).thenReturn(List.of(cartItem));

                // 3. Save & Mapper
                // Mock Repository Save returning the original Entity
                when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArguments()[0]);
                // Mock Mapper returning result
                when(transactionDataMapper.toData(any()))
                                .thenReturn(TransactionResponseData.builder().total(new BigDecimal("200.00")).build());

                com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity addressEntity = new com.fsse2510.fsse2510_project_backend.data.address.entity.ShippingAddressEntity();
                addressEntity.setUser(userEntity);
                when(shippingAddressRepository.findById(1)).thenReturn(java.util.Optional.of(addressEntity));

                // --- Execute ---
                CreateTransactionRequestData request = CreateTransactionRequestData.builder()
                                .user(firebaseUser)
                                .addressId(1)
                                .build();

                TransactionResponseData result = transactionService.createTransaction(request);

                // --- Verify ---
                assertEquals(new BigDecimal("200.00"), result.getTotal());
                verify(cartItemService).getUserCart(firebaseUser);
                verify(transactionRepository).save(any(TransactionEntity.class));
        }
}