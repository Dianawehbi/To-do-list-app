package com.apliman.task_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apliman.task_service.DTO.request.TaskRequestDTO;
import com.apliman.task_service.DTO.request.TaskStatusUpdateDTO;
import com.apliman.task_service.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/tasks")
@RestController
@Tag(name = "Task Controller", description = "API for managing tasks")
public class TaskController {

    // **Tasks**
    // GET    /api/tasks?page=0&size=20&status=&priority=&categoryId=&search=
    // POST   /api/tasks
    // GET    /api/tasks/{id}
    // PUT    /api/tasks/{id}
    // PATCH  /api/tasks/{id}/status        { "status": "DONE" }
    // DELETE /api/tasks/{id}
    // `GET /api/tasks` only ever returns the caller's own tasks. Who the caller is comes from their token, 
    // never from anything in the URL — so nobody can change an ID and see someone else's list.
    @Autowired
    private TaskService taskService;

    // Get filtered tasks 
    @GetMapping
    @Operation(summary = "Get all tasks", description = "Fetches a paginated list of tasks with search, filters, and sorting.")
    public ResponseEntity<?> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            Authentication authentication
    ) {
        return taskService.getFilteredTasks(page, size, categoryId, search, status, priority, authentication);
    }

    @PostMapping
    @Operation(summary = "Create Task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequestDTO dto, Authentication authentication) {
        return taskService.createTask(dto, authentication);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    public ResponseEntity<?> getTaskById(@PathVariable Long id, Authentication authentication) {
        return taskService.getTaskById(id, authentication);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequestDTO dto,
            Authentication authentication) {
        return taskService.updateTask(id, dto, authentication);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Task's status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
             @RequestBody TaskStatusUpdateDTO dto,
            Authentication authentication) {
        return taskService.updateStatus(id, dto, authentication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        return taskService.deleteTask(id, authentication);
    }

}
