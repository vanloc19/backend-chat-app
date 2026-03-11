package com.system.device_service.service;

import com.system.device_service.dto.DeviceResponse;
import com.system.device_service.dto.RegisterDeviceRequest;
import com.system.device_service.model.Device;
import com.system.device_service.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    // ─── Upsert thiết bị khi đăng nhập ───────────────────────
    public void registerDevice(RegisterDeviceRequest request) {
        if (request.getFcmToken() == null || request.getFcmToken().isBlank()) return;

        deviceRepository.findByUserIdAndFcmToken(request.getUserId(), request.getFcmToken())
                .ifPresentOrElse(
                        existing -> {
                            existing.setPlatform(request.getPlatform());
                            existing.setDeviceName(request.getDeviceName());
                            existing.setOsVersion(request.getOsVersion());
                            deviceRepository.save(existing);
                        },
                        () -> deviceRepository.save(Device.builder()
                                .userId(request.getUserId())
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
        deviceRepository.deleteByUserIdAndFcmToken(userId, fcmToken);
    }

    // ─── Helper ───────────────────────────────────────────────
    private DeviceResponse toResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .userId(device.getUserId())
                .platform(device.getPlatform())
                .deviceName(device.getDeviceName())
                .osVersion(device.getOsVersion())
                .fcmToken(device.getFcmToken())
                .lastActiveAt(device.getLastActiveAt())
                .build();
    }
}
