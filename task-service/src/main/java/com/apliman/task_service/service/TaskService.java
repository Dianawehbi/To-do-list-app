package com.apliman.task_service.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.apliman.task_service.DTO.request.TaskRequestDTO;
import com.apliman.task_service.DTO.request.TaskStatusUpdateDTO;
import com.apliman.task_service.DTO.response.IntrospectResponseDTO;
import com.apliman.task_service.exception.ResourceNotFoundException;
import com.apliman.task_service.mapper.TaskMapper;
import com.apliman.task_service.model.Category;
import com.apliman.task_service.model.Task;
import com.apliman.task_service.repository.CategoryRepository;
import com.apliman.task_service.repository.TaskRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private TaskMapper taskMapper;

    private Long currentUserId(Authentication authentication) {
        IntrospectResponseDTO introspect = (IntrospectResponseDTO) authentication.getPrincipal();
        return introspect.getUserId();
    }

    private String currentUserRole(Authentication authentication) {
        IntrospectResponseDTO introspect = (IntrospectResponseDTO) authentication.getPrincipal();
        return introspect.getRole();
    }

    private Task findOwnedTaskOrThrow(Long id, Long currentUserId, String currentUserRole) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!task.getOwnerUserId().equals(currentUserId) && !"ADMIN".equals(currentUserRole)) {
            // 404 instead of 403 so callers can't probe for the existence of other users' tasks
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        return task;
    }

    // ---- endpoints ----
    public ResponseEntity<?> getFilteredTasks(int page, int size, Long categoryId,
            String search, String status, String priority, Authentication authentication) {

        Long currentUserId = currentUserId(authentication);
        String currentUserRole = currentUserRole(authentication);

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!"ADMIN".equals(currentUserRole)) {
                predicates.add(cb.equal(root.get("ownerUserId"), currentUserId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepo.findAll(spec, pageable);

        return ResponseEntity.ok(tasks.map(taskMapper::toDTO));
    }

    public ResponseEntity<?> getTaskById(Long id, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        String currentUserRole = currentUserRole(authentication);
        Task task = findOwnedTaskOrThrow(id, currentUserId, currentUserRole);
        return ResponseEntity.ok(taskMapper.toDTO(task));
    }

    public ResponseEntity<?> createTask(TaskRequestDTO dto, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setOwnerUserId(currentUserId);
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + dto.getCategoryId()));
            task.setCategory(category);
        }

        Task saved = taskRepo.save(task);
        return ResponseEntity.status(201).body(taskMapper.toDTO(saved));
    }

    public ResponseEntity<?> updateTask(Long id, TaskRequestDTO dto, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        String currentUserRole = currentUserRole(authentication);
        // in all cases admin cannot access update task method
        Task task = findOwnedTaskOrThrow(id, currentUserId, currentUserRole);

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + dto.getCategoryId()));
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }

        Task saved = taskRepo.save(task);
        return ResponseEntity.ok(taskMapper.toDTO(saved));
    }

    public ResponseEntity<?> updateStatus(Long id, TaskStatusUpdateDTO dto, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        String currentUserRole = currentUserRole(authentication);
        // in all cases admin cannot access update status method
        Task task = findOwnedTaskOrThrow(id, currentUserId, currentUserRole);

        task.setStatus(dto.getStatus());
        Task saved = taskRepo.save(task);

        return ResponseEntity.ok(taskMapper.toDTO(saved));
    }

    public ResponseEntity<?> deleteTask(Long id, Authentication authentication) {
        Long currentUserId = currentUserId(authentication);
        String currentUserRole = currentUserRole(authentication);
        // in all cases admin cannot access delete method
        Task task = findOwnedTaskOrThrow(id, currentUserId, currentUserRole);

        taskRepo.delete(task);
        return ResponseEntity.noContent().build();
    }
}
