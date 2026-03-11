package com.system.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String OTP_PREFIX      = "otp:";
    private static final String RATE_PREFIX     = "rate_limit:";
    private static final Duration OTP_TTL       = Duration.ofSeconds(120);
    private static final Duration RATE_LIMIT_TTL = Duration.ofSeconds(60);

    // Mock OTP cố định cho môi trường dev
    private static final String MOCK_OTP = "190603";

    private final StringRedisTemplate redis;

    /**
     * Tạo và lưu OTP vào Redis.
     * Mock: luôn trả về "190603".
     */
    public void sendOtp(String phone) {
        if (isRateLimited(phone)) {
            throw new RuntimeException("Vui lòng chờ 60 giây trước khi gửi lại OTP");
        }

        String otp = MOCK_OTP; // TODO: thay bằng SMS provider thật (ESMS, VIETTEL...)

        redis.opsForValue().set(OTP_PREFIX + phone, otp, OTP_TTL);
        redis.opsForValue().set(RATE_PREFIX + phone, "1", RATE_LIMIT_TTL);
    }

    /**
     * Verify OTP và xóa khỏi Redis sau khi dùng (one-time use).
     */
    public boolean verifyOtp(String phone, String inputOtp) {
        String key = OTP_PREFIX + phone;
        String stored = redis.opsForValue().get(key);

        if (stored == null || !stored.equals(inputOtp)) {
            return false;
        }

        return true;
    }

    private boolean isRateLimited(String phone) {
        return Boolean.TRUE.equals(redis.hasKey(RATE_PREFIX + phone));
    }
}
