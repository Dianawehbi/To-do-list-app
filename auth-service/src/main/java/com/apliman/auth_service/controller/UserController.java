package com.apliman.auth_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apliman.auth_service.DTO.request.UserRequestDTO;
import com.apliman.auth_service.DTO.response.UserResponseDTO;
import com.apliman.auth_service.model.UserPrincipal;
import com.apliman.auth_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
@Tag(name = "Token Controller", description = "APIs for token validation and form submission")
public class UserController {

    // GET  `/api/users` 
    // GET  `/api/users/{id}`
    // PUT  `/api/users/{id}` 
    // DELETE  `/api/users/{id}` 
    @Autowired
    private UserService userService;

    // GET - `/api/users` - admin only - List of users, paginated 
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Fetches a paginated list of users.")
    public ResponseEntity<?> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    // GET : /api/users/{id} - admin only - A single user's details 
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get form by id")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // PUT - `/api/users/{id}` - admin only - Updates a user 
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a user")
    public ResponseEntity<UserResponseDTO> updateUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto) {
        return userService.updateUser(principal.getId(), id, dto);

    }

    // DELETE /api/users/{id} - admin only -  Sets `enabled` to false 
    // the row itself is never deleted 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a form")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        return userService.deleteUser(id, authentication.getName());
    }
}
 