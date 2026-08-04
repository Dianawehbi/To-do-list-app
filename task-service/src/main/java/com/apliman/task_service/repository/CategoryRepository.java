package com.apliman.task_service.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apliman.task_service.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Object> {

    Page<Category> findByActive(boolean active, Pageable pageable);

    boolean existsByName(String name);

    Optional<Category> findByName(String name);
}
