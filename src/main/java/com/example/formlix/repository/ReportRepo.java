package com.example.formlix.repository;

import com.example.formlix.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReportRepo extends JpaRepository<Report, Long>
{
    long countByUserId(Long userId);
}
