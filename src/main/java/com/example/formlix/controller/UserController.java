package com.example.formlix.controller;

import com.example.formlix.model.AuthResponse;
import com.example.formlix.model.LoginRequest;
import com.example.formlix.model.RegisterRequest;
import com.example.formlix.model.User;
import com.example.formlix.repository.Userrepo;
import com.example.formlix.service.EmailService;
import com.example.formlix.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final Userrepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse("Email already registered"));
            }

            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            userRepository.save(user);
            emailService.sendRegistrationEmail(user.getEmail(), user.getName());
            String token = jwtUtil.generateToken(user.getEmail());

            // ✅ PROPER RESPONSE WITH ALL USER DATA
            AuthResponse response = new AuthResponse(
                    token,
                    user.getEmail(),
                    user.getName(),
                    user.getId()
            );

            System.out.println("✅ Registration Response: " + response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // ✅ AUTHENTICATE FIRST
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // ✅ FETCH USER DETAILS
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ SEND EMAIL
            emailService.sendLoginEmail(user.getEmail(), user.getName());

            // ✅ GENERATE TOKEN
            String token = jwtUtil.generateToken(user.getEmail());

            // ✅ CREATE RESPONSE WITH CORRECT USER DATA
            AuthResponse response = new AuthResponse(
                    token,
                    user.getEmail(),
                    user.getName(),
                    user.getId()
            );

            // ✅ DEBUG LOG - Check karo console me
            System.out.println("✅ Login successful for user:");
            System.out.println("   ID: " + user.getId());
            System.out.println("   Name: " + user.getName());
            System.out.println("   Email: " + user.getEmail());
            System.out.println("   Token generated: " + token.substring(0, 20) + "...");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid email or password"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AuthResponse response = new AuthResponse(
                    null,
                    user.getEmail(),
                    user.getName(),
                    user.getId()
            );

            System.out.println("✅ /me endpoint called for: " + user.getName() + " (ID: " + user.getId() + ")");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Error fetching user details"));
        }
    }
}
