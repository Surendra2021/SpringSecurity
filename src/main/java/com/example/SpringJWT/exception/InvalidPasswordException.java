package com.example.SpringJWT.exception;

// Thrown when password does not meet the minimum length requirement
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
