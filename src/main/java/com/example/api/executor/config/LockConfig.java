package com.example.api.executor.config;

import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;

@Configuration
public class LockConfig {
    
    @Bean
    public LockRegistry lockRegistry(DataSource dataSource) {
        return new JdbcLockRegistry(new DefaultLockRepository(dataSource));
    }
} 