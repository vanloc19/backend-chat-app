package com.system.auth_service.service;

import com.system.auth_service.dto.AuthResponse;
import com.system.auth_service.dto.LoginRequest;
import com.system.auth_service.dto.RegisterRequest;
import com.system.auth_service.exception.AuthException;
import com.system.auth_service.model.User;
import com.system.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AuthException("Số điện thoại đã được đăng ký");
        }

        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .displayName(request.getDisplayName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new AuthException("Số điện thoại hoặc mật khẩu không đúng"));

        if (!user.isActive()) {
            throw new AuthException("Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Số điện thoại hoặc mật khẩu không đúng");
        }

        String token = jwtService.generateToken(user);
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .phoneNumber(user.getPhoneNumber())
                        .displayName(user.getDisplayName())
                        .avatarUrl(user.getAvatarUrl())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}
