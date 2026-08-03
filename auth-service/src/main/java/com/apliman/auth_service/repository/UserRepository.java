package com.apliman.auth_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apliman.auth_service.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Page<User> findAll(Pageable pageable);
        Optional<User> findByUsername(String username);

        Optional<User> findByEmail(String email);

}

