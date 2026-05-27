package com.edu.sms.modules.auth.repository;

import com.edu.sms.modules.auth.model.AuthUser;
import com.edu.sms.modules.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(AuthUser user);

    void deleteByUser(AuthUser user);

    // ✅ UPSERT: one refresh token per user — atomic, no race condition
    @Modifying
    @Query(value = """
            INSERT INTO auth_refresh_tokens (id, user_id, token, expiry_date)
            VALUES (gen_random_uuid(), :userId, :token, :expiryDate)
            ON CONFLICT (user_id)
            DO UPDATE SET token = :token, expiry_date = :expiryDate
            """, nativeQuery = true)
    void upsert(@Param("userId") UUID userId,
                @Param("token") String token,
                @Param("expiryDate") Instant expiryDate);
}
