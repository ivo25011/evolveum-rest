package com.example.evolveumrest.exception;

/** A user or policy with the same ID already exists. Returned as 409 Conflict. */
public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String message) {
        super(message);
    }
}
