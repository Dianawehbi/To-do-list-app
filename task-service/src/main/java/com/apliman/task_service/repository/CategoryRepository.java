package com.apliman.task_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apliman.task_service.model.Category;

public interface  CategoryRepository extends  JpaRepository<Category, Object>{
    
}
