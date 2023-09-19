package com.foodexplorer.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.CONFLICT)

public class DataConflictException extends  RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;


    public DataConflictException(String message) {
        super(message);
    }

}