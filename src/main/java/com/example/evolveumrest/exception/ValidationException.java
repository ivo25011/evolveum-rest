package com.example.evolveumrest.exception;

/** The request contains invalid data, e.g. an unknown policy condition. Returned as 400 Bad Request. */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
