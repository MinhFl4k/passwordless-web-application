package com.app.demo.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailContextDto {
    String ip;
    String osName;
    String browserName;
    String device;
    String country;
    String city;
}
