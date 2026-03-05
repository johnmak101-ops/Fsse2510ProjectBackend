package com.fsse2510.fsse2510_project_backend.exception.promotion;

public class PromotionNotFoundException extends RuntimeException {
    public PromotionNotFoundException(Integer id) {
        super("Promotion not found: " + id);
    }

    public PromotionNotFoundException(String message) {
        super(message);
    }
}
