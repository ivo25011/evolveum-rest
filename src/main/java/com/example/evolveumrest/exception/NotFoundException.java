package com.example.evolveumrest.exception;

/** The requested user or policy does not exist. Returned as 404 Not Found. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
