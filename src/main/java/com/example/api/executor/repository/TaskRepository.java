package com.example.api.executor.repository;

import java.util.Optional;
import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.api.executor.entity.Task;
import com.example.api.executor.entity.TaskExecution;
import com.example.api.executor.model.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<TaskExecution, Long> {
    Optional<Task> findByTaskId(Long taskId);

    @Modifying
    @Query("UPDATE Task t SET t.state = :status WHERE t.taskId = :taskId")
    void updateTaskStatus(@Param("taskId") Long taskId, @Param("status") TaskStatus status);

    @Modifying
    @Query("UPDATE TaskExecution t SET t.startTime = :startTime WHERE t.taskId = :taskId")
    void updateTaskStartTime(@Param("taskId") Long taskId, @Param("startTime") OffsetDateTime startTime);

    @Modifying
    @Query("UPDATE TaskExecution t SET t.endTime = :endTime WHERE t.taskId = :taskId")
    void updateTaskEndTime(@Param("taskId") Long taskId, @Param("endTime") OffsetDateTime endTime);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TaskExecutionState t WHERE t.taskExecution.taskId = :taskId AND t.state = 'IN_PROGRESS'")
    boolean isTaskInProgress(@Param("taskId") Long taskId);
} 