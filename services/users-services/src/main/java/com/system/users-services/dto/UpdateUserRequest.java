package com.system.users_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 50, message = "Tên hiển thị từ 2-50 ký tự")
    private String displayName;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(regexp = "^(MALE|FEMALE)$", message = "Giới tính phải là MALE hoặc FEMALE")
    private String gender;

    private String avatarUrl;
}
