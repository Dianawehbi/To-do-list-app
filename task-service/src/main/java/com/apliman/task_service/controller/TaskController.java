// package com.apliman.task_service.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;

// import org.springframework.web.bind.annotation.*;

// import com.apliman.task_service.service.TaskService;

// @CrossOrigin(origins = "*", allowedHeaders = "*")
// @RequestMapping("/tasks")
// @RestController
// @Tag(name = "Task Controller", description = "API for managing tasks")
// public class TaskController {

//     // **Tasks**
//     // GET    /api/tasks?page=0&size=20&status=&priority=&categoryId=&search=
//     // POST   /api/tasks
//     // GET    /api/tasks/{id}
//     // PUT    /api/tasks/{id}
//     // PATCH  /api/tasks/{id}/status        { "status": "DONE" }
//     // DELETE /api/tasks/{id}

//     // `GET /api/tasks` only ever returns the caller's own tasks. Who the caller is comes from their token, 
//     // never from anything in the URL — so nobody can change an ID and see someone else's list.

//     @Autowired
//     private TaskService taskService;

//     // Get filtered tasks 
//     @GetMapping
//     @Operation(summary = "Get all tasks", description = "Fetches a paginated list of tasks with search, filters, and sorting.")
//     public ResponseEntity<?> getAllTasks(
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "12") int size,
//             @RequestParam(defaultValue = "12") int categoryId,
//             @RequestParam(required = false, defaultValue = "") String search,
//             @RequestParam(required = false) String status,
//             @RequestParam(required = false) String priority,
//             @RequestParam(required = false, defaultValue = "") String sortOrder
//     ) {
//         return taskService.getFilteredTasks();
//     }

//     // Create a Task
//     @PostMapping
//     @Operation(summary = "Create Task")
//     public ResponseEntity<?> createTask() {
//         return taskService.createTask();
//     }

//     // GET : /tasks/{id}
//     @GetMapping("/{id}")
//     @Operation(summary = "Get task by id")
//     public ResponseEntity<?> getTaskById(@PathVariable Long id) {
//         return taskService.getTaskById(id);
//     }

//     // UPDATE task -  PUT /tasks/{id}
//     @PutMapping("/{id}")
//     @Operation(summary = "Update task")
//     public ResponseEntity<?> updateTask(
//             @PathVariable Long id,
//             @RequestBody TaskRequestDTO dto) {
//         return taskService.updateTask(id, dto);
//     }

//     // PATCH  /api/tasks/{id}/status        { "status": "DONE" }
//     @PatchMapping("/{id}/status")
//     @Operation(summary = "Update Task's status")
//     public ResponseEntity<?> updateStatus(@PathVariable Long id) {
//         return taskService.updateStatus(id);
//     }

//     // DELETE /api/tasks/{id}
//     @DeleteMapping("/{id}")
//     @Operation(summary = "Delete a task")
//     public ResponseEntity<?> delete(@PathVariable Long id) {
//         return taskService.deleteTask(id);
//     }

// }
