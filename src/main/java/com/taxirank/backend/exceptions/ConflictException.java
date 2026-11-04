package com.taxirank.backend.exceptions;

/** The request clashes with current state (duplicate, wrong status). Mapped to 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
