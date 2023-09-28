package com.menufoods.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnathourizedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UnathourizedException(String message) {
        super(message);
    }
}