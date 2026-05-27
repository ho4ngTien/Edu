package com.edu.sms.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn; // ms

    // User info (embed để FE không cần gọi thêm API)
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private UUID id;
        private String email;
        private String username;
        private String role; // role chính
    }
}
