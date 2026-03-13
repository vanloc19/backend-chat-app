package com.system.device_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DeviceResponse {
    private String id;
    private String userId;
    private String deviceId;
    private String platform;
    private String deviceName;
    private String osVersion;
    private String fcmToken;
    private Instant lastActiveAt;
}
