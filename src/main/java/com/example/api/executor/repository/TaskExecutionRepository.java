package com.example.api.executor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.api.executor.entity.TaskExecution;
import java.util.Optional;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {
    // Basic CRUD operations are provided by JpaRepository
    Optional<TaskExecution> findByTaskId(Long taskId);
} 