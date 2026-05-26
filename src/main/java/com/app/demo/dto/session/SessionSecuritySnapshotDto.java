package com.app.demo.dto.session;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionSecuritySnapshotDto implements Serializable {
    UUID userId;
    String email;
    String userAgent;
    String ip;
    String country;
    String city;

    LocalDateTime issuedAt;
}

