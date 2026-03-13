package com.system.auth_service.service;

import com.system.auth_service.client.DeviceServiceClient;
import com.system.auth_service.dto.AuthResponse;
import com.system.auth_service.dto.DeviceInfo;
import com.system.auth_service.dto.LoginRequest;
import com.system.auth_service.dto.RegisterRequest;
import com.system.auth_service.dto.VerifyOtpRequest;
import com.system.auth_service.exception.AuthException;
import com.system.auth_service.model.RefreshToken;
import com.system.auth_service.model.User;
import com.system.auth_service.repository.RefreshTokenRepository;
import com.system.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final DeviceServiceClient deviceServiceClient;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ─── OTP: gửi ────────────────────────────────────────────
    public void sendOtp(String phoneNumber) {
        otpService.sendOtp(phoneNumber);
    }
    // ─── Đăng ký: xác thực OTP + tạo user với password ─────────────────
    public AuthResponse register(RegisterRequest request) {
        if (!otpService.verifyOtp(request.getPhoneNumber(), request.getOtp())) {
            throw new AuthException("OTP không hợp lệ hoặc đã hết hạn");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AuthException("Số điện thoại đã được đăng ký");
        }

        User user = userRepository.save(User.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
            .gender(request.getGender())
                .displayName(request.getDisplayName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build());

        deviceServiceClient.registerDevice(user.getId(), request.getDeviceInfo());
        return buildAuthResponse(user);
    }
    // ─── OTP: xác thực → tạo user nếu chưa tồn tại ──────────
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        if (!otpService.verifyOtp(request.getPhoneNumber(), request.getOtp())) {
            throw new AuthException("OTP không hợp lệ hoặc đã hết hạn");
        }

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .phoneNumber(request.getPhoneNumber())
                                .isActive(true)
                                .build()
                ));

        deviceServiceClient.registerDevice(user.getId(), request.getDeviceInfo());
        return buildAuthResponse(user);
    }

    // ─── Login bằng password ──────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new AuthException("Số điện thoại hoặc mật khẩu không đúng"));

        if (!user.isActive()) {
            throw new AuthException("Tài khoản đã bị khóa");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Số điện thoại hoặc mật khẩu không đúng");
        }

        deviceServiceClient.registerDevice(user.getId(), request.getDeviceInfo());
        return buildAuthResponse(user);
    }

    // ─── Refresh access token ─────────────────────────────────
    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new AuthException("Refresh token không hợp lệ"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new AuthException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AuthException("Người dùng không tồn tại"));

        // Rotate refresh token
        refreshTokenRepository.delete(stored);
        return buildAuthResponse(user);
    }

    // ─── Logout ───────────────────────────────────────────────
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.deleteByToken(refreshTokenStr);
    }

    // ─── Helper ───────────────────────────────────────────────
    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .createdAt(Instant.now())
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .phoneNumber(user.getPhoneNumber())
                    .gender(user.getGender())
                        .displayName(user.getDisplayName())
                        .avatarUrl(user.getAvatarUrl())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}
