package com.app.demo.model;

import com.app.demo.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36, nullable = false, updatable = false)
    UUID id;

    String name;

    @Column(unique = true)
    String email;
    String phone;
    String password;
    String secret;

    @Enumerated(EnumType.STRING)
    AuthProvider provider;

    String providerId;

    @Column(nullable = false)
    Integer passwordFailedAttempts = 0;

    @Column(nullable = false)
    Integer otpFailedAttempts = 0;

    LocalDateTime lockedUntil;

    @Column(nullable = false)
    private boolean verified;

    @ManyToMany
    Set<Role> roles;
}
