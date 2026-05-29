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

    @NotBlank(message = "Name must not be blank", groups = BasicValidation.class)
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters", groups = BasicValidation.class)
    String name;

    @NotBlank(message = "Email must not be blank", groups = BasicValidation.class)
    @Email(message = "Invalid email format", groups = BasicValidation.class)
//    @UniqueEmail(groups = AdvancedValidation.class)
    String email;

    @Pattern(regexp = "\\d*", message = "Phone number must contain only digits", groups = BasicValidation.class)
    String phone;

    @NotBlank(message = "Password must not be blank", groups = BasicValidation.class)
    @Size(min = 8, message = "Password must be at least 8 characters", groups = BasicValidation.class)
    @ValidPassword(groups = AdvancedValidation.class)
    String password;

    @NotBlank(message = "Confirm password must not be blank", groups = BasicValidation.class)
    String confirmPassword;
}
