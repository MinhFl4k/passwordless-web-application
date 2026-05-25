package com.app.demo.dto.request;

import com.app.demo.validation.UniqueEmailForUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@UniqueEmailForUpdate
public class UserUpdateDto {

    Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email;

    @Pattern(regexp = "\\d*", message = "Phone number must contain only digits")
    String phone;
}
