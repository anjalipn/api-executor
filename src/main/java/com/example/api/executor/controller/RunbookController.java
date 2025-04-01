package com.example.api.executor.controller;

import com.example.api.executor.entity.Runbook;
import com.example.api.executor.mapper.RunbookMapper;
import com.example.api.executor.model.RunbookResponse;
import com.example.api.executor.repository.RunbookRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/runbooks")
public class RunbookController {
    
    private final RunbookRepository runbookRepository;
    private final RunbookMapper runbookMapper;

    public RunbookController(RunbookRepository runbookRepository, RunbookMapper runbookMapper) {
        this.runbookRepository = runbookRepository;
        this.runbookMapper = runbookMapper;
    }

    @GetMapping
    public ResponseEntity<List<RunbookResponse>> getAllRunbooks() {
        List<RunbookResponse> runbooks = runbookRepository.findAll().stream()
            .map(runbookMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(runbooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RunbookResponse> getRunbook(@PathVariable Long id) {
        return runbookRepository.findById(id)
            .map(runbookMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
} 