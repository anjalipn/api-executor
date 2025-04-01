package com.example.api.executor.service;

import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskLockService {
    private final LockRegistry lockRegistry;
    
    @Value("${task.lock.timeout.seconds:30}")
    private long lockTimeoutSeconds;

    public boolean tryLock(Long taskId) {
        String lockId = taskId.toString();
        Lock lock = lockRegistry.obtain(lockId);
        try {
            boolean locked = lock.tryLock(lockTimeoutSeconds, TimeUnit.SECONDS);
            if (locked) {
                log.info("Lock acquired - Task ID: {}", taskId);
            } else {
                log.warn("Lock is already held by another process - Task ID: {}", taskId);
            }
            return locked;
        } catch (InterruptedException e) {
            log.error("Interrupted while trying to acquire lock - Task ID: {}", taskId, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(Long taskId) {
        String lockId = taskId.toString();
        Lock lock = lockRegistry.obtain(lockId);
        if (lock.tryLock()) {
            try {
                lock.unlock();
                log.info("Lock released - Task ID: {}", taskId);
            } catch (IllegalStateException e) {
                log.error("Error unlocking - Task ID: {}", taskId, e);
            }
        } else {
            log.warn("Failed to unlock - Task ID: {}", taskId);
        }
    }
} 