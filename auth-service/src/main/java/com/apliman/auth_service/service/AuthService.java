package com.apliman.auth_service.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.apliman.auth_service.DTO.request.IntrospectRequestDTO;
import com.apliman.auth_service.DTO.request.LoginRequestDTO;
import com.apliman.auth_service.DTO.request.RefreshTokenRequestDTO;
import com.apliman.auth_service.DTO.request.RegisterRequestDTO;
import com.apliman.auth_service.DTO.response.AuthResponseDTO;
import com.apliman.auth_service.DTO.response.IntrospectResponseDTO;
import com.apliman.auth_service.exception.DuplicateResourceException;
import com.apliman.auth_service.exception.InvalidCredentialsException;
import com.apliman.auth_service.exception.InvalidTokenException;
import com.apliman.auth_service.exception.ResourceNotFoundException;
import com.apliman.auth_service.mapper.UserMapper;
import com.apliman.auth_service.model.RefreshToken;
import com.apliman.auth_service.model.User;
import com.apliman.auth_service.repository.UserRepository;

// import com.apliman.auth_service.DTO.RegisterRequestDTO;
// public class AuthService {
//    public  ResponseEntity<?> introspect(){
//       return ResponseEntity.status(HttpStatus.OK).body(data);
//     }
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ResponseEntity<?> login(LoginRequestDTO dto) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        } catch (DisabledException e) {
            throw new InvalidCredentialsException("This account has been disabled. Please contact support.");
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        String accessToken = jwtService.generateToken(dto.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(dto.getUsername());

        long expiresAt = Instant.now().plusMillis(jwtService.getAccessTokenExpirationMs()).getEpochSecond();

        AuthResponseDTO authResponse = new AuthResponseDTO();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken.getToken());
        authResponse.setAccessTokenExpiresAt(expiresAt);

        return ResponseEntity.ok(authResponse);
    }

    public ResponseEntity<?> register(RegisterRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already in use");

        }
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already in use");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPasswordHash(encoder.encode(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(savedUser));
    }

    public ResponseEntity<?> logout(RefreshTokenRequestDTO dto) {
        Optional<RefreshToken> tokenOpt = refreshTokenService.findByToken(dto.getRefreshToken());
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.ok("Logged out");
        }
        refreshTokenService.revoke(tokenOpt.get());
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    public ResponseEntity<?> refresh(RefreshTokenRequestDTO dto) {
        RefreshToken tokenRecord = refreshTokenService.findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token. Please log in again."));

        RefreshToken validToken = refreshTokenService.verifyExpiration(tokenRecord);

        String username = validToken.getUser().getUsername();
        String newAccessToken = jwtService.generateToken(username);
        long expiresAt = Instant.now().plusMillis(jwtService.getAccessTokenExpirationMs()).getEpochSecond();

        AuthResponseDTO authResponse = new AuthResponseDTO();
        authResponse.setAccessToken(newAccessToken);
        authResponse.setRefreshToken(validToken.getToken());
        authResponse.setAccessTokenExpiresAt(expiresAt);

        return ResponseEntity.ok(authResponse);
    }

    public ResponseEntity<?> getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    public ResponseEntity<IntrospectResponseDTO> introspect(IntrospectRequestDTO dto) {
        try {
            String username = jwtService.extractUserName(dto.getToken());
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null || !user.getEnabled()) {
                return ResponseEntity.ok(IntrospectResponseDTO.inactive());
            }

            if (!jwtService.validateToken(dto.getToken(), userDetailsService.loadUserByUsername(username))) {
                return ResponseEntity.ok(IntrospectResponseDTO.inactive());
            }

            Instant expiresAt = jwtService.extractExpiration(dto.getToken()).toInstant();

            IntrospectResponseDTO response = new IntrospectResponseDTO(
                    true, user.getId(), user.getUsername(), user.getRole(), expiresAt);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.ok(IntrospectResponseDTO.inactive());
        }
    }
}
