package com.fsse2510.fsse2510_project_backend.data.user.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateUserProfileRequestDto {
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    // [Updated] 支援國際格式 (包括括號)，且必須以數字結尾 (防止結尾係橫線或空格)
    // ^ : 開始
    // \\+? : 可選的 + 號
    // [0-9\\-\\s\\(\\)]{6,29}: 中間部分容許 數字、橫線、空格、括號
    // [0-9] : 結尾必須係數字
    // $ : 結束
    @Pattern(regexp = "^\\+?[0-9\\-\\s\\(\\)]{6,29}[0-9]$", message = "Invalid phone number format")
    private String phoneNumber;
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;
    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;
}