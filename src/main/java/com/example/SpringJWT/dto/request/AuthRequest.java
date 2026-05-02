package com.example.SpringJWT.dto.request;

import lombok.Data;

/**
 * AuthRequest is the body the client sends to /api/auth/login.
 */
@Data
public class AuthRequest {
    private String username;
    private String password;
}


