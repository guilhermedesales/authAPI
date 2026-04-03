package com.api.auth.Application.Exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

