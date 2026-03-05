package com.fsse2510.fsse2510_project_backend.exception.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TransactionIllegalStateException extends RuntimeException {
    public TransactionIllegalStateException(String message) {
        super(message);
    }
}
