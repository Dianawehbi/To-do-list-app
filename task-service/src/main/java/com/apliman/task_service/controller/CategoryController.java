package com.apliman.task_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apliman.task_service.DTO.request.CategoryRequestDTO;
import com.apliman.task_service.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/categories")
@RestController
@Tag(name = "Category Controller", description = "API for managing categories")
public class CategoryController {

    // **categories**
    // GET    /api/categories?active=true
    // POST   /api/categories               admin only
    // GET    /api/categories/{id}
    // PUT    /api/categories/{id}          admin only
    // DELETE /api/categories/{id}          admin only
    // Every list endpoint is paginated on the server side, using Spring's standard paging format.
    @Autowired
    private CategoryService categoryService;

    // Get filtered categories 
    @GetMapping
    @Operation(summary = "Get all categories", description = "Fetches a paginated list of categories with an active/inactive filter")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Boolean active) {
        return categoryService.getAllCategories(page, size, active);
    }

    // GET : /categories/{id }
    @GetMapping("/{id}")
    @Operation(summary = "Get Ctaregory by id")
    public ResponseEntity<?> getCategoryById(@Valid @PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    // Create category - admin only
    @PostMapping
    @Operation(summary = "Create Category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequestDTO dto) {
        return categoryService.createCategory(dto);
    }

    // UPDATE category -  PUT /categories/{id}  -    admin only
    @PutMapping("/{id}")
    @Operation(summary = "Update Category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        return categoryService.updateCategory(id, dto);
    }

    // DELETE /api/categories/{id}        admin only
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
