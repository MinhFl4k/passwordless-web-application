package com.app.demo.model;

import com.app.demo.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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

    LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private boolean verified;

    @ManyToMany
    Set<Role> roles;
}
