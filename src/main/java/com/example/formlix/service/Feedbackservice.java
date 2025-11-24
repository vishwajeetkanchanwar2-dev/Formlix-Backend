package com.example.formlix.service;

import com.example.formlix.model.Feedback;
import com.example.formlix.model.User;
import com.example.formlix.repository.Feedbackrepo;
import com.example.formlix.repository.Userrepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Feedbackservice {

    private final Feedbackrepo feedbackRepository;
    private final Userrepo userRepository;

    // Save feedback
    public Feedback saveFeedback(Long userId, String message, int rating) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Feedback feedback = Feedback.builder()
                .user(user)
                .message(message)
                .rating(rating)
                .createdAt(LocalDateTime.now())
                .build();

        return feedbackRepository.save(feedback);
    }

    // Get all feedbacks
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }
}
