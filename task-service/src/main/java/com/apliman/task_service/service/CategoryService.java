package com.apliman.task_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.apliman.task_service.model.Category;
import com.apliman.task_service.repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepo;

    // get all users
    public ResponseEntity<Page<Category>> getAllCategories(int page, int size, Boolean active) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Category> categories = (active != null)
                ? categoryRepo.findByActive(active, pageable)
                : categoryRepo.findAll(pageable);

        return ResponseEntity.ok(categories);
    }

    // get user by id
    // public ResponseEntity<UserResponseDTO> getCategoryById(Long id) {
    //     User user = userRepo.findById(id)
    //             .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    //     return ResponseEntity.ok(userMapper.toDto(user));
    // }
}
