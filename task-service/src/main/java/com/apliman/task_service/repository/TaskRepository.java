package com.apliman.task_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apliman.task_service.model.Task;

public interface TaskRepository extends  JpaRepository<Task, Object>{
    
}
