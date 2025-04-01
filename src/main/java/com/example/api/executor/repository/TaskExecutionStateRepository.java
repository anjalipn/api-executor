package com.example.api.executor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.api.executor.entity.TaskExecutionState;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskExecutionStateRepository extends JpaRepository<TaskExecutionState, Long> {
    @Query("SELECT s FROM TaskExecutionState s WHERE s.taskExecution.taskId = :taskId ORDER BY s.createdAt DESC")
    List<TaskExecutionState> findLatestStatesByTaskId(Long taskId);

    @Query("SELECT s FROM TaskExecutionState s WHERE s.taskExecution.taskId = :taskId ORDER BY s.createdAt DESC LIMIT 1")
    Optional<TaskExecutionState> findLatestStateByTaskId(Long taskId);
} 