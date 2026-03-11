package com.system.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[3-9][0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotBlank(message = "OTP không được để trống")
    private String otp;

    // Optional — có thể null nếu client không gửi
    private DeviceInfo deviceInfo;
}
