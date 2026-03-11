package com.system.auth_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;

    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private String id;
        private String phoneNumber;
        private String displayName;
        private String avatarUrl;
        private String email;
    }
}
