package com.app.demo.model;

import com.app.demo.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "Role")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    RoleEnum name;

    @Column(nullable = false)
    String description;
}
