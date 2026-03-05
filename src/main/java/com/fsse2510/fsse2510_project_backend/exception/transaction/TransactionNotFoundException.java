package com.fsse2510.fsse2510_project_backend.exception.transaction;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}