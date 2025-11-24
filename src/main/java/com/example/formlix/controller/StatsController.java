package com.example.formlix.controller;

import com.example.formlix.repository.ReportRepo;
import com.example.formlix.repository.Userrepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatsController {

    private final ReportRepo reportRepository;
    private final Userrepo userRepository;

    /**
     * Get total reports count
     * Public endpoint - accessible without login
     */
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Long>> getReportsCount() {
        try {
            long count = reportRepository.count();
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Long> response = new HashMap<>();
            response.put("count", 0L);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get total users count
     * Public endpoint - accessible without login
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Long>> getUsersCount() {
        try {
            long count = userRepository.count();
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Long> response = new HashMap<>();
            response.put("count", 0L);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get all stats at once
     * Public endpoint - shows all reports if not logged in
     * Shows user-specific reports if logged in
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            boolean isAuthenticated = authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal());

            if (isAuthenticated) {
                String userEmail = authentication.getName();
                System.out.println("✅ Authenticated User Email: " + userEmail);

                var user = userRepository.findByEmail(userEmail).orElse(null);

                if (user != null) {
                    System.out.println("✅ User Found - ID: " + user.getId() + ", Name: " + user.getName());

                    long userReports = reportRepository.countByUserId(user.getId());
                    System.out.println("✅ User Reports Count: " + userReports);

                    // User-specific stats
                    stats.put("totalReports", userReports);
                    stats.put("isUserSpecific", true);
                    stats.put("userId", user.getId());
                    stats.put("userName", user.getName());
                    stats.put("userEmail", userEmail);
                } else {
                    System.out.println("❌ User not found with email: " + userEmail);
                    stats.put("totalReports", 0L);
                    stats.put("isUserSpecific", false);
                    stats.put("error", "User not found");
                }
            } else {
                System.out.println("🔓 Anonymous user - showing global stats");
                // Global stats for non-authenticated users
                stats.put("totalReports", reportRepository.count());
                stats.put("isUserSpecific", false);
            }

            // Always include total users count
            stats.put("totalUsers", userRepository.count());
            stats.put("success", true);

            System.out.println("📊 Stats Response: " + stats);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            System.err.println("❌ Error in getAllStats: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReports", 0L);
            stats.put("totalUsers", 0L);
            stats.put("success", false);
            stats.put("error", e.getMessage());
            return ResponseEntity.ok(stats);
        }
    }

    /**
     * Get total users count - Simple and clear
     * Public endpoint - accessible without login
     */
    @GetMapping("/total-users")
    public ResponseEntity<Map<String, Object>> getTotalUsers() {
        try {
            long totalUsers = userRepository.count();

            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", totalUsers);
            response.put("success", true);

            System.out.println("👥 Total Users in System: " + totalUsers);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error getting total users: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", 0L);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get user-specific stats (requires authentication)
     * Shows detailed stats for logged-in user
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyStats(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not authenticated");
                return ResponseEntity.ok(response);
            }

            String userEmail = authentication.getName();
            var user = userRepository.findByEmail(userEmail).orElse(null);

            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "User not found");
                return ResponseEntity.ok(response);
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("userId", user.getId());
            stats.put("userName", user.getName());
            stats.put("userEmail", user.getEmail());
            stats.put("myReports", reportRepository.countByUserId(user.getId()));
            stats.put("totalReportsInSystem", reportRepository.count());
            stats.put("totalUsers", userRepository.count());
            stats.put("success", true);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}