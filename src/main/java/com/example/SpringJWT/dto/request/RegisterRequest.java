package com.example.SpringJWT.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;

    // User can send multiple roles e.g. ["ADMIN", "USER"]
    // If empty or null, defaults to ["USER"] in AuthService
    private List<String> roles;
}
