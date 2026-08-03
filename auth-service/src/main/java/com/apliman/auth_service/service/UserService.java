package com.apliman.auth_service.service;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.apliman.auth_service.DTO.request.UserRequestDTO;
import com.apliman.auth_service.DTO.response.UserResponseDTO;
import com.apliman.auth_service.exception.DuplicateResourceException;
import com.apliman.auth_service.exception.ResourceNotFoundException;
import com.apliman.auth_service.exception.SelfActionNotAllowedException;
import com.apliman.auth_service.mapper.UserMapper;
import com.apliman.auth_service.model.User;
import com.apliman.auth_service.repository.RefreshTokenRepository;
import com.apliman.auth_service.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenrepo;

    public UserService(UserRepository userRepo, UserMapper userMapper, RefreshTokenRepository refreshTokenrepo) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
        this.refreshTokenrepo = refreshTokenrepo;
    }

    // get all users
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable) {
        Page<UserResponseDTO> users = userRepo.findAll(pageable)
                .map(userMapper::toDto);
        return ResponseEntity.ok(users);
    }

    // get user by id
    public ResponseEntity<UserResponseDTO> getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    // update user
    public ResponseEntity<UserResponseDTO> updateUser(Long id, UserRequestDTO dto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepo.findByUsername(dto.getUsername())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Username already in use: " + dto.getUsername());
                });

        userRepo.findByEmail(dto.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Email already in use: " + dto.getEmail());
                });

        if (!dto.getEnabled() && Objects.equals(user.getId(), id)) {
            throw new SelfActionNotAllowedException("You cannot disable your own account");
        }

        userMapper.updateEntityFromDto(dto, user);
        User updatedUser = userRepo.save(user);

        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    // delete user | disable 
    public ResponseEntity<Void> deleteUser(Long id, String currentUsername) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getUsername().equals(currentUsername)) {
            throw new SelfActionNotAllowedException("You cannot disable your own account");
        }

        // refreshTokenrepo.findByUser(user);
        user.setEnabled(false);
        userRepo.save(user);
        return ResponseEntity.noContent().build();
    }
}
