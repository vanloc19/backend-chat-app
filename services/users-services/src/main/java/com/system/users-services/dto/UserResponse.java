package com.system.users_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String phoneNumber;
    private String email;
    private String displayName;
    private String avatarUrl;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
