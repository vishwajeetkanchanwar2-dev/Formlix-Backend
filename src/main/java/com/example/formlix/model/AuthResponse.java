package com.example.formlix.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String username;  // ✅ This will be the display name
    private String message;

    // Constructor for success with token
    public AuthResponse(String token, String email, String username) {
        this.token = token;
        this.email = email;
        this.username = username;  // ✅ Pass name here
        this.message = "Authentication successful";
    }

    // Constructor for error message
    public AuthResponse(String message) {
        this.message = message;
    }
}