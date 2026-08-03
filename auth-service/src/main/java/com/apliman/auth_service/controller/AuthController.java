package com.apliman.auth_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apliman.auth_service.DTO.request.IntrospectRequestDTO;
import com.apliman.auth_service.DTO.request.LoginRequestDTO;
import com.apliman.auth_service.DTO.request.RefreshTokenRequestDTO;
import com.apliman.auth_service.DTO.request.RegisterRequestDTO;
import com.apliman.auth_service.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Token Controller", description = "APIs for token validation and form submission")
public class AuthController {

    // POST | `/api/auth/register` | Creates a new account 
    // POST | `/api/auth/login` | anyone | Returns an access token and a refresh token 
    // POST | `/api/auth/refresh` | anyone | Exchanges a refresh token for a new access token 
    // POST | `/api/auth/logout` | logged in | Revokes the refresh token 
    // POST | `/api/auth/introspect` | internal only | Checks whether a token is valid — covered in more detail below 
    // GET  | `/api/auth/me` | logged in | Returns the current user's own profile 

    @Autowired
    private AuthService authService;

    // POST  /api/auth/register
    @PostMapping("/register")
    @Operation(summary = "Creates a new account")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return authService.register(dto);
    }

    // POST | /api/auth/login
    @PostMapping("/login")
    @Operation(summary = "Returns an access token and a refresh token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    // POST : /api/auth/refresh
    @PostMapping("/refresh")
    @Operation(summary = "Exchanges a refresh token for a new access token")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequestDTO dto) {
        return authService.refresh(dto);
    }

    // POST | `/api/auth/logout
    @PostMapping("/logout")
    @Operation(summary = "Revokes the refresh token")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequestDTO dto) {
        return authService.logout(dto);
    }

    // POST | `/api/auth/introspect - internal only | Checks whether a token is valid
    @PostMapping("/introspect")
    @Operation(summary = "Checks whether a token is valid")
    public ResponseEntity<?> introspect(@RequestBody IntrospectRequestDTO dto) {
        return authService.introspect(dto);
    }
    
    // GET | /api/auth/me
    @GetMapping("/me")
    @Operation(summary = "Returns the current user's own profile")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }
}
