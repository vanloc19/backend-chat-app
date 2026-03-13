package com.system.users_service.controller;

import com.system.users_service.dto.UpdateUserRequest;
import com.system.users_service.dto.UserResponse;
import com.system.users_service.exception.UserException;
import com.system.users_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── Lấy user hiện tại từ JWT header ────────────────────
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@RequestHeader(name = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(userService.getById(requiredUserId(userId)));
    }

    // ─── Lấy user theo ID ─────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // ─── Lấy user theo số điện thoại ─────────────────────────
    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<UserResponse> getByPhoneNumber(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(userService.getByPhoneNumber(phoneNumber));
    }

    // ─── Cập nhật thông tin profile ───────────────────────────
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(requiredUserId(userId), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ─── Health ───────────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Users service is running");
    }

    private String requiredUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new UserException("Thiếu thông tin người dùng");
        }
        return userId;
    }
}
