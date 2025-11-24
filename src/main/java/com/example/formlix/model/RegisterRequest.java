package com.example.formlix.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;      // ✅ User's actual name
    private String email;     // ✅ For login
    private String password;
}