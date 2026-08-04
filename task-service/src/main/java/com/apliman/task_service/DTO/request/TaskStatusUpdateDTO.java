package com.apliman.task_service.DTO.request;

import com.apliman.task_service.model.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    private TaskStatus status;
}