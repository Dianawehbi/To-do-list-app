package com.apliman.task_service.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.apliman.task_service.DTO.request.CategoryRequestDTO;
import com.apliman.task_service.exception.ActionNotAllowedException;
import com.apliman.task_service.exception.DuplicateResourceException;
import com.apliman.task_service.exception.ResourceNotFoundException;
import com.apliman.task_service.model.Category;
import com.apliman.task_service.repository.CategoryRepository;
import com.apliman.task_service.repository.TaskRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private TaskRepository taskRepo;

    // get all categories
    public ResponseEntity<Page<Category>> getAllCategories(int page, int size, Boolean active) {
        Pageable pageable = PageRequest.of(page, size);

        Integer activeFlag = (active != null) ? (active ? 1 : 0) : null;

        Page<Category> categories = (activeFlag  != null)
                ? categoryRepo.findByActive(activeFlag, pageable)
                : categoryRepo.findAll(pageable);

        return ResponseEntity.ok(categories);
    }

    // get category by ID
    public ResponseEntity<Category> getCategoryById(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return ResponseEntity.ok(category);
    }

    // create new category
    public ResponseEntity<?> createCategory(CategoryRequestDTO dto) {
        if (categoryRepo.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Category with name '" + dto.getName() + "' already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setColor(dto.getColor());
        category.setActive(dto.getActive() ? 1 : 0);

        Category saved = categoryRepo.save(category);
        return ResponseEntity.status(201).body(saved);
    }

    // update category
    public ResponseEntity<Category> updateCategory(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        Optional<Category> categ = categoryRepo.findByName(dto.getName());
        if (categ.isPresent() && !Objects.equals(categ.get().getId(), id)) {
            throw new DuplicateResourceException("Category with name '" + dto.getName() + "' already exists");
        }

        category.setName(dto.getName());
        category.setColor(dto.getColor());
        category.setActive(dto.getActive() ? 1 : 0);

        Category updated = categoryRepo.save(category);
        return ResponseEntity.ok(updated);
    }

    // delete category
    public ResponseEntity<Void> deleteCategory(Long id) {
        //  A category that's referenced by at least one task can't be deleted 
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        boolean inUse = taskRepo.existsByCategoryId(id);
        if (inUse) {
            throw new ActionNotAllowedException("Category is in use and cannot be deleted. Please deactivate it instead.");
        }

        categoryRepo.delete(category);
        return ResponseEntity.noContent().build();
    }

}
