package com.example.SpringJWT.exception;

// Thrown when someone tries to register with a username or email that already exists
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
