package com.edu.sms.modules.auth.service;

import com.edu.sms.common.security.JwtService;
import com.edu.sms.modules.auth.dto.AuthResponse;
import com.edu.sms.modules.auth.dto.LoginRequest;
import com.edu.sms.modules.auth.dto.RefreshTokenRequest;
import com.edu.sms.modules.auth.dto.RegisterRequest;
import com.edu.sms.modules.auth.model.AuthUser;
import com.edu.sms.modules.auth.model.RefreshToken;
import com.edu.sms.modules.auth.model.Role;
import com.edu.sms.modules.auth.repository.AuthUserRepository;
import com.edu.sms.modules.auth.repository.RefreshTokenRepository;
import com.edu.sms.modules.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ─── Register ─────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được đăng ký");
        }
        if (authUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new IllegalStateException("Vai trò ROLE_STUDENT chưa được khởi tạo trong DB"));

        AuthUser user = AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(studentRole))
                .isActive(true)
                .build();

        AuthUser saved = authUserRepository.save(user);
        String accessToken = jwtService.generateToken(saved);

        upsertRefreshToken(saved);
        String refreshToken = refreshTokenRepository.findByUser(saved)
                .map(RefreshToken::getToken)
                .orElseThrow();

        return buildAuthResponse(saved, accessToken, refreshToken);
    }

    // ─── Login ────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // authenticate() gọi loadUserByUsername() bên trong
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // ✅ Cast principal trực tiếp — tránh DB lookup lần 2
        AuthUser user = (AuthUser) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(user);
        upsertRefreshToken(user);
        String refreshToken = refreshTokenRepository.findByUser(user)
                .map(RefreshToken::getToken)
                .orElseThrow();

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ─── Refresh Token ────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token không hợp lệ"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        AuthUser user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return buildAuthResponse(user, newAccessToken, refreshToken.getToken());
    }

    // ─── Logout ───────────────────────────────────────────────────────────

    @Transactional
    public void logout(UUID userId) {
        authUserRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private void upsertRefreshToken(AuthUser user) {
        String tokenValue = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(604_800_000L); // 7 ngày
        refreshTokenRepository.upsert(user.getId(), tokenValue, expiryDate);
    }

    private AuthResponse buildAuthResponse(AuthUser user, String accessToken, String refreshToken) {
        String role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("ROLE_STUDENT");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .role(role)
                        .build())
                .build();
    }
}
