package com.system.device_service.dto;

import lombok.Data;

@Data
public class RegisterDeviceRequest {
    private String userId;
    private String platform;
    private String deviceName;
    private String osVersion;
    private String fcmToken;
}
