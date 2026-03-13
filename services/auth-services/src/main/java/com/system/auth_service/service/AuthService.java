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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final DeviceServiceClient deviceServiceClient;
    private final MongoTemplate mongoTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @PostConstruct
    void cleanupLegacyRefreshTokenSchema() {
        Update update = new Update()
                .unset("phoneNumber")
                .unset("passwordHash")
                .unset("displayName")
                .unset("gender")
                .unset("email")
                .unset("avatarUrl")
                .unset("isActive")
                .unset("token");
        mongoTemplate.updateMulti(new Query(), update, RefreshToken.class);
    }

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
            return issueAuthTokens(user, resolveDeviceId(request.getDeviceInfo()));
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
            return issueAuthTokens(user, resolveDeviceId(request.getDeviceInfo()));
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
        return issueAuthTokens(user, resolveDeviceId(request.getDeviceInfo()));
    }

    // ─── Refresh access token ─────────────────────────────────
    public AuthResponse refresh(String refreshTokenStr) {
        String tokenHash = hashToken(refreshTokenStr);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("Refresh token không hợp lệ"));

        if (stored.isRevoked()) {
            throw new AuthException("Refresh token đã bị thu hồi");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AuthException("Người dùng không tồn tại"));

        String newRefreshToken = jwtService.generateRefreshToken();
        stored.setTokenHash(hashToken(newRefreshToken));
        stored.setRevoked(false);
        stored.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        stored.setCreatedAt(Instant.now());
        refreshTokenRepository.save(stored);

        return buildAuthResponse(user, newRefreshToken);
    }

    // ─── Logout ───────────────────────────────────────────────
    public void logout(String refreshTokenStr) {
        String tokenHash = hashToken(refreshTokenStr);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(this::revokeToken);
    }

    // ─── Helper ───────────────────────────────────────────────
    private AuthResponse issueAuthTokens(User user, String deviceId) {
        String normalizedDeviceId = normalizeDeviceId(deviceId);
        String userId = user.getId();
        String refreshToken = jwtService.generateRefreshToken();

        RefreshToken session = refreshTokenRepository
            .findByUserIdAndDeviceId(userId, normalizedDeviceId)
                .orElseGet(() -> RefreshToken.builder()
                .userId(userId)
                        .deviceId(normalizedDeviceId)
                        .build());

        session.setTokenHash(hashToken(refreshToken));
        session.setRevoked(false);
        session.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        session.setCreatedAt(Instant.now());
        refreshTokenRepository.save(session);

        return buildAuthResponse(user, refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String refreshToken) {
        String accessToken = jwtService.generateAccessToken(user);

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

    private void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    private String resolveDeviceId(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return "unknown-device";
        }
        if (hasText(deviceInfo.getDeviceId())) {
            return deviceInfo.getDeviceId().trim();
        }
        if (hasText(deviceInfo.getFcmToken())) {
            return "fcm:" + deviceInfo.getFcmToken().trim();
        }
        if (hasText(deviceInfo.getPlatform()) || hasText(deviceInfo.getDeviceName()) || hasText(deviceInfo.getOsVersion())) {
            String fingerprint = String.format(
                    "%s|%s|%s",
                    nullToEmpty(deviceInfo.getPlatform()),
                    nullToEmpty(deviceInfo.getDeviceName()),
                    nullToEmpty(deviceInfo.getOsVersion())
            );
            return "fp:" + hashToken(fingerprint);
        }
        return "unknown-device";
    }

    private String normalizeDeviceId(String deviceId) {
        return hasText(deviceId) ? deviceId.trim() : "unknown-device";
    }

    private String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
