package com.example.api.executor.mapper;

import com.example.api.executor.entity.Runbook;
import com.example.api.executor.entity.Task;
import com.example.api.executor.model.RunbookResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RunbookMapper {
    
    public RunbookResponse toResponse(Runbook runbook) {
        RunbookResponse response = new RunbookResponse();
        response.setRunbookId(runbook.getRunbookId());
        response.setRunbookDescription(runbook.getRunbookDescription());
        response.setChangeRecord(runbook.getChangeRecord());
        response.setCreator(runbook.getCreator());
        response.setScheduledTime(runbook.getScheduledTime());
        response.setState(runbook.getState());
        response.setCreationMethod(runbook.getCreationMethod());
        response.setIsScheduled(runbook.getIsScheduled());
        response.setCreatedAt(runbook.getCreatedAt());
        response.setCreatedBy(runbook.getCreatedBy());
        
        if (runbook.getTasks() != null) {
            List<RunbookResponse.TaskResponse> taskResponses = runbook.getTasks().stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
            response.setTasks(taskResponses);
        }
        
        return response;
    }

    private RunbookResponse.TaskResponse toTaskResponse(Task task) {
        RunbookResponse.TaskResponse response = new RunbookResponse.TaskResponse();
        response.setTaskId(task.getTaskId());
        response.setPosition(task.getPosition());
        response.setType(task.getType());
        response.setTaskDescription(task.getTaskDescription());
        response.setState(task.getState());
        response.setCreatedAt(task.getCreatedAt());
        response.setCreatedBy(task.getCreatedBy());
        
        if (task.getRequest() != null) {
            RunbookResponse.TaskRequest request = new RunbookResponse.TaskRequest();
            request.setUrl(task.getRequest().getUrl());
            request.setMethod(task.getRequest().getMethod());
            request.setData(task.getRequest().getData());
            response.setRequest(request);
        }
        
        return response;
    }
} 