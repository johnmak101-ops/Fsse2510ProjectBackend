package com.fsse2510.fsse2510_project_backend.exception;

import com.fsse2510.fsse2510_project_backend.exception.cartitem.CartItemNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.InvalidQuantityException;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.NotEnoughStockException;
import com.fsse2510.fsse2510_project_backend.exception.cartitem.CartEmptyException;
import com.fsse2510.fsse2510_project_backend.exception.coupon.CouponInvalidException;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductAlreadyExistedException;
import com.fsse2510.fsse2510_project_backend.exception.product.ProductNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.transaction.TransactionNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.transaction.TransactionIllegalStateException;
import com.fsse2510.fsse2510_project_backend.exception.transaction.PaymentVerificationException;
import com.fsse2510.fsse2510_project_backend.exception.transaction.IllegalPaymentOperationException;
import com.fsse2510.fsse2510_project_backend.exception.user.UserNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.promotion.PromotionNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.promotion.PromotionValidationException;
import com.fsse2510.fsse2510_project_backend.exception.coupon.CouponAlreadyExistsException;
import com.fsse2510.fsse2510_project_backend.exception.address.AddressNotFoundException;
import com.fsse2510.fsse2510_project_backend.exception.address.MissingShippingAddressException;
import com.fsse2510.fsse2510_project_backend.exception.stripe.InvalidStripeSignatureException;
import com.fsse2510.fsse2510_project_backend.exception.stripe.StripeWebhookBusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // --- 404 Not Found Group ---
    @ExceptionHandler({
            ProductNotFoundException.class,
            CartItemNotFoundException.class,
            TransactionNotFoundException.class,
            UserNotFoundException.class,
            AddressNotFoundException.class,
            PromotionNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        logger.debug("Resource Not Found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- 400 Bad Request Group ---
    @ExceptionHandler({
            InvalidQuantityException.class,
            NotEnoughStockException.class,
            ProductAlreadyExistedException.class,
            IllegalArgumentException.class,
            CouponInvalidException.class,
            CartEmptyException.class,
            MissingShippingAddressException.class,
            PaymentVerificationException.class,
            IllegalPaymentOperationException.class,
            PromotionValidationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        logger.debug("Bad Request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- 409 Conflict Group ---
    @ExceptionHandler({
            TransactionIllegalStateException.class,
            CouponAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        logger.debug("Conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- 400 Bad Request Group (Validation) ---
    // Handles @Valid annotation failures — extracts the first binding error message
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        logger.debug("Validation Error: {}", errorMessage);
        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // --- 403 Forbidden Group ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access Denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission.");
    }

    // --- 503 Service Unavailable Group ---
    @ExceptionHandler(ProviderException.class)
    public ResponseEntity<ErrorResponse> handleProviderException(ProviderException ex) {
        logger.error("Provider Error: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE,
                "Service is currently unavailable. Please try again later.");
    }

    // --- Stripe Webhook Errors ---
    @ExceptionHandler(InvalidStripeSignatureException.class)
    public ResponseEntity<String> handleInvalidStripeSignature(InvalidStripeSignatureException ex) {
        logger.warn("Invalid Stripe Signature: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("invalid signature");
    }

    @ExceptionHandler(StripeWebhookBusinessException.class)
    public ResponseEntity<String> handleStripeBusinessException(StripeWebhookBusinessException ex) {
        logger.warn("Stripe Business Logic Interruption (returning 200 OK to stop retries): {}", ex.getMessage());
        return ResponseEntity.ok("ignored business error");
    }

    // --- 500 Internal Server Error ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        logger.error("Unexpected Error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please contact support.");
    }

    // --- Helper Method ---
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    // DTO for Error Response
    public record ErrorResponse(int status, String message) {
    }
}
