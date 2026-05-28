package com.app.demo.dto.request;

import com.app.demo.validation.ExistingEmail;
import com.app.demo.validation.sequence.AdvancedValidation;
import com.app.demo.validation.sequence.BasicValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailReqDto {

    @NotBlank(message = "Email must not be blank", groups = BasicValidation.class)
    @Email(message = "Invalid email format", groups = AdvancedValidation.class)
//    @ExistingEmail(groups = AdvancedValidation.class)
    private String email;
}
