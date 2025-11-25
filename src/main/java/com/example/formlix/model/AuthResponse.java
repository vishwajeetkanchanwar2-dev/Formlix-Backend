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
    private String username;  // Display name
    private String message;
    private Long id;  // ✅ USER ID ADD KIYA

    // Constructor for success with token (OLD - backward compatibility)
    public AuthResponse(String token, String email, String username) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.message = "Authentication successful";
    }

    // ✅ NEW Constructor with USER ID
    public AuthResponse(String token, String email, String username, Long id) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.id = id;  // ✅ ID include kiya
        this.message = "Authentication successful";
    }

    // Constructor for error message
    public AuthResponse(String message) {
        this.message = message;
    }
}
