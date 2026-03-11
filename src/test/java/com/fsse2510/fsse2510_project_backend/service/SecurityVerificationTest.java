package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.transaction.entity.TransactionEntity;
import com.fsse2510.fsse2510_project_backend.data.transaction.status.PaymentStatus;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.exception.transaction.PaymentVerificationException;
import com.fsse2510.fsse2510_project_backend.mapper.transaction.TransactionDataMapper;
import com.fsse2510.fsse2510_project_backend.repository.TransactionRepository;
import com.fsse2510.fsse2510_project_backend.service.impl.TransactionFinalizationService;
import com.fsse2510.fsse2510_project_backend.service.impl.TransactionServiceImpl;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityVerificationTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserService userService;
    @Mock
    private TransactionDataMapper transactionDataMapper;
    @Mock
    private StripeService stripeService;
    @Mock
    private TransactionFinalizationService finalizationService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionEntity transaction;
    private final String FIREBASE_UID = "test_user";
    private final Integer TID = 1;

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity();
        user.setUid(100);
        user.setFirebaseUid(FIREBASE_UID);

        transaction = new TransactionEntity();
        transaction.setTid(TID);
        transaction.setUser(user);
        transaction.setTotal(new BigDecimal("100.00"));
        transaction.setStatus(PaymentStatus.PENDING);

        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testFinishTransaction_AmountMismatch_ShouldThrowException() {
        UserData userData = mock(UserData.class);
        when(userData.getUid()).thenReturn(100);
        when(userService.getOrCreateUser(any())).thenReturn(userData);

        Session session = mock(Session.class);
        when(session.getClientReferenceId()).thenReturn("1");
        when(session.getPaymentIntent()).thenReturn("pi_123");
        when(stripeService.retrieveSession("test_session")).thenReturn(session);

        PaymentIntent intent = mock(PaymentIntent.class);
        // Stripe uses cents. 5000 cents = $50.00. Transaction total is $100.00.
        when(intent.getAmount()).thenReturn(5000L);
        when(intent.getStatus()).thenReturn("requires_capture");
        when(stripeService.retrievePaymentIntent("pi_123")).thenReturn(intent);

        when(transactionRepository.findByTidAndUser_Uid(anyInt(), anyInt())).thenReturn(Optional.of(transaction));

        assertThrows(PaymentVerificationException.class, () -> {
            transactionService.finishTransaction(new FirebaseUserData(FIREBASE_UID, null), TID, "test_session");
        });
    }

    @Test
    void testFinishFromWebhook_NullIntentId_ShouldAutoAssociate() {
        String newIntentId = "pi_999";
        transaction.setStripePaymentIntentId(null);

        UserData userData = mock(UserData.class);
        when(userData.getUid()).thenReturn(100);
        when(userService.getOrCreateUser(any())).thenReturn(userData);

        when(transactionRepository.findByTidAndUser_Uid(anyInt(), anyInt())).thenReturn(Optional.of(transaction));

        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getStatus()).thenReturn("succeeded");
        // Transaction is 100.00, Stripe amount is 10000 cents
        when(intent.getAmount()).thenReturn(10000L); 
        when(intent.getCurrency()).thenReturn("hkd");
        
        when(stripeService.retrievePaymentIntent(newIntentId)).thenReturn(intent);

        transactionService.finishTransactionFromStripeWebhook(FIREBASE_UID, TID, newIntentId, 10000L, "hkd");

        assertEquals(newIntentId, transaction.getStripePaymentIntentId());
        verify(transactionRepository, atLeastOnce()).save(transaction);
    }
}
