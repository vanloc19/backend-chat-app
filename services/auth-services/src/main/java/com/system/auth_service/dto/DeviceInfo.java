package com.system.auth_service.dto;

import lombok.Data;

@Data
public class DeviceInfo {
    private String deviceId;
    private String platform;   // ANDROID, IOS, WEB
    private String deviceName;
    private String osVersion;
    private String fcmToken;   // Firebase push notification token
}
