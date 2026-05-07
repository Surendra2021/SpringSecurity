package com.example.SpringJWT.controller;

import com.example.SpringJWT.exception.InvalidPasswordException;
import com.example.SpringJWT.exception.UserAlreadyExistsException;
import com.example.SpringJWT.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409 Conflict — username or email already taken
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 404 Not Found — user doesn't exist
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 400 Bad Request — password too short
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 500 Internal Server Error — anything unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        String message;
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            message = e.getMessage();
        } else {
            message = "An unexpected error occurred";
        }
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
