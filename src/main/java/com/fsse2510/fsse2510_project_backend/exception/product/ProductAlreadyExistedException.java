package com.fsse2510.fsse2510_project_backend.exception.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductAlreadyExistedException extends RuntimeException {
    public ProductAlreadyExistedException(String name) {
        super("Product already exists: " + name);
    }
}
