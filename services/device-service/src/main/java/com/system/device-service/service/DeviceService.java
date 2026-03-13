package com.system.device_service.service;

import com.system.device_service.dto.DeviceResponse;
import com.system.device_service.dto.RegisterDeviceRequest;
import com.system.device_service.model.Device;
import com.system.device_service.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.HexFormat;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    // ─── Upsert thiết bị khi đăng nhập ───────────────────────
    public void registerDevice(RegisterDeviceRequest request) {
        if (!hasText(request.getUserId())) {
            return;
        }

        String resolvedDeviceId = resolveDeviceId(request);
        if (!hasText(resolvedDeviceId)) {
            return;
        }

        deviceRepository.findByUserIdAndDeviceId(request.getUserId(), resolvedDeviceId)
                .ifPresentOrElse(
                        existing -> {
                            existing.setDeviceId(resolvedDeviceId);
                            existing.setPlatform(request.getPlatform());
                            existing.setDeviceName(request.getDeviceName());
                            existing.setOsVersion(request.getOsVersion());
                            existing.setFcmToken(request.getFcmToken());
                            deviceRepository.save(existing);
                        },
                        () -> deviceRepository.save(Device.builder()
                                .userId(request.getUserId())
                                .deviceId(resolvedDeviceId)
                                .platform(request.getPlatform())
                                .deviceName(request.getDeviceName())
                                .osVersion(request.getOsVersion())
                                .fcmToken(request.getFcmToken())
                                .build())
                );
    }

    // ─── Lấy tất cả thiết bị của 1 user ──────────────────────
    public List<DeviceResponse> getDevicesByUserId(String userId) {
        return deviceRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Xóa thiết bị (logout khỏi thiết bị đó) ─────────────
    public void removeDevice(String userId, String fcmToken) {
        if (!hasText(userId) || !hasText(fcmToken)) {
            return;
        }

        // Backward compatible: client may pass either fcmToken or deviceId.
        deviceRepository.deleteByUserIdAndFcmToken(userId, fcmToken);
        deviceRepository.deleteByUserIdAndDeviceId(userId, fcmToken);
    }

    // ─── Helper ───────────────────────────────────────────────
    private DeviceResponse toResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .userId(device.getUserId())
                .deviceId(device.getDeviceId())
                .platform(device.getPlatform())
                .deviceName(device.getDeviceName())
                .osVersion(device.getOsVersion())
                .fcmToken(device.getFcmToken())
                .lastActiveAt(device.getLastActiveAt())
                .build();
    }

    private String resolveDeviceId(RegisterDeviceRequest request) {
        if (hasText(request.getDeviceId())) {
            return request.getDeviceId().trim();
        }

        if (hasText(request.getFcmToken())) {
            return "fcm:" + request.getFcmToken().trim();
        }

        String platform = safe(request.getPlatform());
        String name = safe(request.getDeviceName());
        String osVersion = safe(request.getOsVersion());
        if (platform.isBlank() && name.isBlank() && osVersion.isBlank()) {
            return null;
        }

        return "fp:" + sha256(platform + "|" + name + "|" + osVersion);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
