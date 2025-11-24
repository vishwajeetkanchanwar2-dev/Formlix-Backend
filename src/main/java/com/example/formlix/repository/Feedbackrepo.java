package com.example.formlix.repository;

import com.example.formlix.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Feedbackrepo extends JpaRepository<Feedback, Long>
{

}
