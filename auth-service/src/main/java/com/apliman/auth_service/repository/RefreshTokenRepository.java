package com.apliman.auth_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apliman.auth_service.model.RefreshToken;
import com.apliman.auth_service.model.User;

import jakarta.transaction.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    @Modifying
    @Transactional
    @Query("update RefreshToken r set r.revoked = true where r.user.id = :userId")
    int revokeByUserId(@Param("userId") long userId);

}
