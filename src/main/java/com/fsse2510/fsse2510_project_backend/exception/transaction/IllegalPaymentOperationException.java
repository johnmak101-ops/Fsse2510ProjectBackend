package com.fsse2510.fsse2510_project_backend.exception.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalPaymentOperationException extends RuntimeException {
    public IllegalPaymentOperationException(String message) {
        super(message);
    }
}
