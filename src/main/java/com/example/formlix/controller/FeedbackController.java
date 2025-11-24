package com.example.formlix.controller;

import com.example.formlix.model.Feedback;
import com.example.formlix.service.Feedbackservice;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController
{
    private final Feedbackservice feedbackService;

    // Create feedback
    @PostMapping("/create")
    public ResponseEntity<Feedback> createFeedback(@RequestBody FeedbackRequest request) {
        Feedback feedback = feedbackService.saveFeedback(request.getUserId(), request.getMessage(), request.getRating());
        return ResponseEntity.ok(feedback);
    }

    // Get all feedbacks
    @GetMapping("/all")
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    // Request DTO
    @Data
    public static class FeedbackRequest {
        private Long userId;
        private String message;
        private int rating; // 1-5 scale
    }
}
