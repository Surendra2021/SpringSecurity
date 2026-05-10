package com.example.SpringJWT.dto.response;

import com.example.SpringJWT.entity.User;

public class UserResponse {

    private String username;
    private String email;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // Maps User entity → UserResponse in one place
    // so controller doesn't need to do it manually
    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        return response;
    }
}
