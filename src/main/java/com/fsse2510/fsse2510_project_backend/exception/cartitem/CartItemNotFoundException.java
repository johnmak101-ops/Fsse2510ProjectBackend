package com.fsse2510.fsse2510_project_backend.exception.cartitem;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String message) {
        super(message);
    }
}
