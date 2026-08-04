package com.apliman.task_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apliman.task_service.model.Task;

public interface TaskRepository extends JpaRepository<Task, Object> {

    boolean existsByCategoryId(Long categoryId);

    public Page<Task> findAll(Specification<Task> spec, Pageable pageable);

}
