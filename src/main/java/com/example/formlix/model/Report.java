package com.example.formlix.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    private String formatType; // pdf, docx, pptx

    private String filePath;

    // ✅ NEW: Store page limit for tracking
    private Integer pageLimit;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    private User user;
}