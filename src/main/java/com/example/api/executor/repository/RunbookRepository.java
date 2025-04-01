package com.example.api.executor.repository;

import com.example.api.executor.entity.Runbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunbookRepository extends JpaRepository<Runbook, Long> {
} 