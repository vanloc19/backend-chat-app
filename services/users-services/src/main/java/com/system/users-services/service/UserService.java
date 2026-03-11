package com.system.users_service.service;

import com.system.users_service.dto.UpdateUserRequest;
import com.system.users_service.dto.UserResponse;
import com.system.users_service.exception.UserException;
import com.system.users_service.model.User;
import com.system.users_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Người dùng không tồn tại"));
        return toResponse(user);
    }

    public UserResponse getByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserException("Người dùng không tồn tại"));
        return toResponse(user);
    }

    public UserResponse updateUser(String id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Người dùng không tồn tại"));

        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        return toResponse(userRepository.save(user));
    }

    // ─── Helper ───────────────────────────────────────────────
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
