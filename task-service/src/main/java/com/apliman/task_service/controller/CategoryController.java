package com.apliman.task_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apliman.task_service.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/categories")
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
public ResponseEntity<Page<Category>> getAllCategories(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(required = false) Boolean active) {
    return categoryService.getAllCategories(page, size, active);
}

//     // Create category
//     @PostMapping
//     @Operation(summary = "Create Category")
//     public ResponseEntity<?> createCategory() {
//         return categoryService.createCategory();
//     }
//     // GET : /categories/{id}
//     @GetMapping("/{id}")
//     @Operation(summary = "Get Ctaregory by id")
//     public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
//         return categoryService.getCategoryById(id);
//     }
//     // UPDATE category -  PUT /categories/{id}  -    admin only
//     @PutMapping("/{id}")
//     @Operation(summary = "Update Category")
//     public ResponseEntity<?> updateCategory(
//             @PathVariable Long id,
//             @RequestBody CategoryRequestDTO dto) {
//         return categoryService.updateCategory(id, dto);
//     }
//     // DELETE /api/categories/{id}        admin only
//     @DeleteMapping("/{id}")
//     @Operation(summary = "Delete category")
//     public ResponseEntity<?> delete(@PathVariable Long id) {
//         return categoryService.deleteCategory(id);
//     }
}
