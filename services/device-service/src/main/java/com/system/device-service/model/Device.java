package com.system.device_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "devices")
@CompoundIndex(name = "userId_fcmToken", def = "{'userId': 1, 'fcmToken': 1}", unique = true)
public class Device {

    @Id
    private String id;

    private String userId;

    private String platform;   // ANDROID, IOS, WEB

    private String deviceName;

    private String osVersion;

    private String fcmToken;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant lastActiveAt;
}
