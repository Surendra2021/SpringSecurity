package com.example.SpringJWT.dto.response;

import com.example.SpringJWT.entity.User;

// DTO — Data Transfer Object
// Only exposes username and email to the client
// password, role, id are intentionally excluded for security
public class UserResponse {

    // fields — store the data
    private String username;
    private String email;

    // getter method — returns the value of username field
    public String getUsername() {
        return username;
    }

    // setter method — sets the value of username field
    // MapStruct calls this when mapping User → UserResponse
    public void setUsername(String username) {
        this.username = username;
    }

    // getter method — returns the value of email field
    public String getEmail() {
        return email;
    }

    // setter method — sets the value of email field
    // MapStruct calls this when mapping User → UserResponse
    public void setEmail(String email) {
        this.email = email;
    }

    // old manual mapping method — replaced by MapStruct (UserMapper.toUserResponse)
    // MapStruct auto-generates the same logic at compile time
    /*
    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        return response;
    }
    */
}
