package com.app.demo.dto.request;

import com.app.demo.validation.PasswordMatches;
import com.app.demo.validation.UniqueEmail;
import com.app.demo.validation.ValidPassword;
import com.app.demo.validation.sequence.AdvancedValidation;
import com.app.demo.validation.sequence.BasicValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@PasswordMatches(first = "password", second = "confirmPassword")
public class UserSignupDto {

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters", groups = BasicValidation.class)
    String name;

    @UniqueEmail
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email;

    @Pattern(regexp = "\\d*", message = "Phone number must contain only digits")
    String phone;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters", groups = BasicValidation.class)
    @ValidPassword(groups = AdvancedValidation.class)
    String password;

    @NotBlank(message = "Confirm password must not be blank")
    String confirmPassword;
}
