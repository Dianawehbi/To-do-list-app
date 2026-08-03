package com.apliman.auth_service.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apliman.auth_service.exception.InvalidTokenException;
import com.apliman.auth_service.exception.ResourceNotFoundException;
import com.apliman.auth_service.model.RefreshToken;
import com.apliman.auth_service.model.User;
import com.apliman.auth_service.repository.RefreshTokenRepository;
import com.apliman.auth_service.repository.UserRepository;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${auth.refresh-expiry-days}")
    private long refreshExpiryDays;

    public RefreshToken createRefreshToken(String username) {
        // User user = userRepository.findByUsername(username)
        //         .orElseThrow(() -> new RuntimeException("User not found: " + username));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS));

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked. Please login again.");
        }
        return token;
    }

    public RefreshToken revoke(RefreshToken token) {
        token.setRevoked(true);
        return refreshTokenRepository.save(token);
    }
}
