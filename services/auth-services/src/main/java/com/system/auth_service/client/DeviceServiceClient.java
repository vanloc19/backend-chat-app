package com.system.auth_service.client;

import com.system.auth_service.dto.DeviceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.device.url}")
    private String deviceServiceUrl;

    /**
     * Gửi thông tin thiết bị sang device-service sau khi đăng nhập thành công.
     * Không throw exception — lỗi device không ảnh hưởng luồng auth.
     */
    public void registerDevice(String userId, DeviceInfo deviceInfo) {
        if (deviceInfo == null) return;

        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("platform", deviceInfo.getPlatform());
            payload.put("deviceName", deviceInfo.getDeviceName());
            payload.put("osVersion", deviceInfo.getOsVersion());
            payload.put("fcmToken", deviceInfo.getFcmToken());

            restTemplate.postForObject(
                    deviceServiceUrl + "/api/devices/register",
                    payload,
                    Void.class);
        } catch (Exception e) {
            log.warn("Không thể đăng ký thiết bị cho userId={}: {}", userId, e.getMessage());
        }
    }
}
