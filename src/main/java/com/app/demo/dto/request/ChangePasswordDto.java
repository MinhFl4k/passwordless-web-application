package com.app.demo.dto.request;

import com.app.demo.validation.CurrentPassword;
import com.app.demo.validation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@PasswordMatches(first = "newPassword", second = "confirmPassword")
public class ChangePasswordDto {
    @NotBlank(message = "Current password must not be blank")
    @CurrentPassword
    String currentPassword;

    @NotBlank(message = "New password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword;

    @NotBlank(message = "Confirm password must not be blank")
    String confirmPassword;
}
