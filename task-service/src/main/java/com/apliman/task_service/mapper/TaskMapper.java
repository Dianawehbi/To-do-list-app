package com.apliman.task_service.mapper;

import org.mapstruct.Mapper;

import com.apliman.task_service.DTO.response.TaskResponseDTO;
import com.apliman.task_service.model.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskResponseDTO toDTO(Task task);

    
    // private TaskResponseDTO toDTO(Task task) {
    //     TaskResponseDTO dto = new TaskResponseDTO();
    //     dto.setId(task.getId());
    //     dto.setTitle(task.getTitle());
    //     dto.setDescription(task.getDescription());
    //     dto.setStatus(task.getStatus());
    //     dto.setPriority(task.getPriority());
    //     dto.setDueDate(task.getDueDate());
    //     if (task.getCategory() != null) {
    //         dto.setCategoryId(task.getCategory().getId());
    //         dto.setCategoryName(task.getCategory().getName());
    //     }
    //     dto.setCreatedAt(task.getCreatedAt());
    //     dto.setUpdatedAt(task.getUpdatedAt());
    //     return dto;
    // }
}
