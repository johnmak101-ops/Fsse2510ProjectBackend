package com.fsse2510.fsse2510_project_backend.exception.cartitem;


public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException(String message) {
        super(message);
    }
}
