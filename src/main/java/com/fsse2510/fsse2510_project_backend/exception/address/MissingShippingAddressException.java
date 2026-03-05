package com.fsse2510.fsse2510_project_backend.exception.address;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingShippingAddressException extends RuntimeException {
    public MissingShippingAddressException(String message) {
        super(message);
    }
}
