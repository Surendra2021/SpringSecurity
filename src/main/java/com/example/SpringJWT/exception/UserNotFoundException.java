package com.example.SpringJWT.exception;

// Thrown when a user is looked up but doesn't exist in the database
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
