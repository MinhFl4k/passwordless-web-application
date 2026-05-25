package com.app.demo.config;

import com.app.demo.enums.RoleEnum;
import com.app.demo.model.Role;
import com.app.demo.repository.RoleRepository;
import com.app.demo.repository.UserRepository;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;

@Configuration
public class AppConfig {

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            Role adminRole = roleRepository.findByName(RoleEnum.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(RoleEnum.ROLE_ADMIN)
                            .description("Admin role")
                            .build()));

            Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(RoleEnum.ROLE_USER)
                            .description("User role")
                            .build()));

            Role guestRole = roleRepository.findByName(RoleEnum.ROLE_GUEST)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(RoleEnum.ROLE_GUEST)
                            .description("Guest role")
                            .build()));
        };
    }

    @Bean
    public UserAgentAnalyzer userAgentAnalyzer() {
        return UserAgentAnalyzer
                .newBuilder()
                .withCache(10000)
                .immediateInitialization()
                .withField("OperatingSystemName")
                .withField("AgentName")
                .withField("DeviceClass")
                .build();
    }

    @Bean
    public JdbcPublicKeyCredentialUserEntityRepository jdbcPublicKeyCredentialRepository(JdbcOperations jdbc) {
        return new JdbcPublicKeyCredentialUserEntityRepository(jdbc);
    }

    @Bean
    public JdbcUserCredentialRepository jdbcUserCredentialRepository(JdbcOperations jdbc) {
        return new JdbcUserCredentialRepository(jdbc);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
