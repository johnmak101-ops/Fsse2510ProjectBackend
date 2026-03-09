package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.payment.domainObject.response.PaymentResponseData;
import com.fsse2510.fsse2510_project_backend.data.payment.dto.response.PaymentResponseDto;
import com.fsse2510.fsse2510_project_backend.data.transaction.dto.request.CreateTransactionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.transaction.dto.response.TransactionResponseDto;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.mapper.payment.PaymentDtoMapper;
import com.fsse2510.fsse2510_project_backend.mapper.transaction.TransactionDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for managing user transactions.
 * <p>
 * Handles transaction lifecycle: creation, details retrieval, payment
 * preparation, and completion/abortion.
 * </p>
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionDtoMapper transactionDtoMapper;
    private final PaymentDtoMapper paymentDtoMapper;

    private FirebaseUserData getFirebaseUser(JwtAuthenticationToken token) {
        return FirebaseUserData.builder()
                .firebaseUid(token.getToken().getSubject())
                .email((String) token.getTokenAttributes().get("email"))
                .build();
    }

    /**
     * Retrieves all transactions associated with the authenticated user.
     *
     * @param token The JWT authentication token.
     * @return A list of transaction DTOs.
     */
    @GetMapping
    public List<TransactionResponseDto> getTransactions(JwtAuthenticationToken token) {
        return transactionService.getTransactions(getFirebaseUser(token)).stream()
                .map(transactionDtoMapper::toDto)
                .toList();
    }

    /**
     * Creates a new purchase transaction.
     * <p>
     * This initializes the order based on the user's current cart state (implicit
     * or explicit).
     * </p>
     *
     * @param token      The JWT authentication token.
     * @param requestDto Optional request body (can be empty).
     * @return The created transaction DTO.
     */
    @PostMapping
    public TransactionResponseDto createTransaction(JwtAuthenticationToken token,
            @Valid @RequestBody(required = false) CreateTransactionRequestDto requestDto) {
        var requestData = transactionDtoMapper.toRequestData(
                requestDto != null ? requestDto : new CreateTransactionRequestDto(), getFirebaseUser(token));
        return transactionDtoMapper.toDto(transactionService.createTransaction(requestData));
    }

    /**
     * Retrieves detailed information about a specific transaction.
     *
     * @param tid   The transaction ID.
     * @param token The JWT authentication token.
     * @return The transaction detail DTO.
     */
    @GetMapping("/{tid}")
    public TransactionResponseDto getTransactionById(@PathVariable Integer tid,
            JwtAuthenticationToken token) {
        return transactionDtoMapper.toDto(
                transactionService.getTransactionById(getFirebaseUser(token), tid));
    }

    /**
     * Prepares the payment intent (e.g., Stripe) for a transaction.
     * <p>
     * This locks the transaction and returns payment details to the client.
     * </p>
     *
     * @param tid   The transaction ID.
     * @param token The JWT authentication token.
     * @return Payment response DTO containing client secret/payment link.
     */
    @PostMapping("/{tid}/payment")
    public PaymentResponseDto preparePayment(@PathVariable Integer tid, JwtAuthenticationToken token) {
        PaymentResponseData paymentResponseData = transactionService.preparePayment(getFirebaseUser(token), tid);
        return paymentDtoMapper.toDto(paymentResponseData);
    }

    /**
     * Marks a transaction as successfully paid.
     * <p>
     * Should be called after the payment provider confirms success.
     * </p>
     *
     * @param tid   The transaction ID.
     * @param token The JWT authentication token.
     * @return The updated transaction DTO.
     */
    @PatchMapping("/{tid}/success")
    public TransactionResponseDto finishTransaction(@PathVariable Integer tid, JwtAuthenticationToken token,
            @RequestParam(required = false) String session_id) {
        return transactionDtoMapper.toDto(
                transactionService.finishTransaction(getFirebaseUser(token), tid, session_id));
    }

    /**
     * Aborts or cancels a transaction.
     * <p>
     * Useful if payment fails or user cancels the checkout.
     * </p>
     *
     * @param tid   The transaction ID.
     * @param token The JWT authentication token.
     * @return The updated transaction DTO.
     */
    @PatchMapping("/{tid}/fail")
    public TransactionResponseDto abortTransaction(@PathVariable Integer tid, JwtAuthenticationToken token) {
        return transactionDtoMapper.toDto(
                transactionService.abortTransaction(getFirebaseUser(token), tid));
    }

    /**
     * Updates the transaction status to PROCESSING.
     * <p>
     * Used by the frontend to indicate the user has landed on the success page
     * and finalization is underway.
     * </p>
     *
     * @param tid   The transaction ID.
     * @param token The JWT authentication token.
     * @return The updated transaction DTO.
     */
    @PatchMapping("/{tid}/processing")
    public TransactionResponseDto updateStatusToProcessing(@PathVariable Integer tid, JwtAuthenticationToken token) {
        return transactionDtoMapper.toDto(
                transactionService.updateStatusToProcessing(getFirebaseUser(token), tid));
    }
}