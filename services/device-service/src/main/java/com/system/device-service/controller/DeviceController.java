package com.system.device_service.controller;

import com.system.device_service.dto.DeviceResponse;
import com.system.device_service.dto.RegisterDeviceRequest;
import com.system.device_service.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // ─── Gọi từ auth-service sau khi đăng nhập ───────────────
    @PostMapping("/register")
    public ResponseEntity<Void> registerDevice(@RequestBody RegisterDeviceRequest request) {
        deviceService.registerDevice(request);
        return ResponseEntity.ok().build();
    }

    // ─── Lấy tất cả thiết bị của user ────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceResponse>> getDevices(@PathVariable String userId) {
        return ResponseEntity.ok(deviceService.getDevicesByUserId(userId));
    }

    // ─── Xóa thiết bị (logout khỏi 1 device) ─────────────────
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> removeDevice(
            @PathVariable String userId,
            @RequestParam String fcmToken) {
        deviceService.removeDevice(userId, fcmToken);
        return ResponseEntity.noContent().build();
    }

    // ─── Health ───────────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Device service is running");
    }
}
