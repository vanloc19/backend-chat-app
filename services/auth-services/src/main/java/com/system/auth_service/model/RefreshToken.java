package com.system.auth_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
@CompoundIndex(name = "user_device_unique", def = "{'userId': 1, 'deviceId': 1}", unique = true)
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String tokenHash;

    private String deviceId;

    @Builder.Default
    private boolean revoked = false;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    private Instant createdAt;
}
