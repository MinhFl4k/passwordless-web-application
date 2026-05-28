package com.app.demo.service.Impl;

import com.app.demo.dto.common.CustomUserDetails;
import com.app.demo.dto.request.ChangePasswordDto;
import com.app.demo.dto.request.UserSignupDto;
import com.app.demo.dto.request.UserUpdateDto;
import com.app.demo.dto.response.UserResDto;
import com.app.demo.enums.*;
import com.app.demo.model.User;
import com.app.demo.repository.RoleRepository;
import com.app.demo.repository.UserRepository;
import com.app.demo.service.RequestContextFingerprintService;
import com.app.demo.service.UserTokenService;
import com.app.demo.service.UserService;
import com.app.demo.util.TotpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserTokenService tokenLoginService;

    private final PasswordEncoder passwordEncoder;

    private final RequestContextFingerprintService requestContextFingerprintService;

    @Override
    public void updateAuthenticationPrincipal(User savedUser, Authentication authentication) {
        if (savedUser == null) {
            return;
        }

        CustomUserDetails newPrincipal = new CustomUserDetails(savedUser);

        UsernamePasswordAuthenticationToken newAuthentication =
                new UsernamePasswordAuthenticationToken(
                        newPrincipal,
                        null,
                        newPrincipal.getAuthorities()
                );

        if (authentication != null) {
            newAuthentication.setDetails(authentication.getDetails());
        }

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }

    @Override
    public void signupUser(UserSignupDto userSignupDto) throws IllegalArgumentException {

        String email = userSignupDto.getEmail();

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessage.EMAIL_REQUIRED.getMessage());
        }

        if(userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(ErrorMessage.EMAIL_EXIST.getMessage());
        }

        User user = new User();
        user.setName(userSignupDto.getName());
        user.setEmail(email);
        user.setPhone(userSignupDto.getPhone());
        user.setPassword(passwordEncoder.encode(userSignupDto.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setVerified(false);
        user.setSecret(TotpUtil.generateSecret());

        var role = roleRepository.findByName(RoleEnum.ROLE_GUEST)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.ROLE_NOT_FOUND.getMessage()));

        user.setRoles(Set.of(role));

        tokenLoginService.sendUserTokenLink(email, UserTokenType.VERIFY);

        userRepository.save(user);
    }

    @Override
    public User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage());
        }

        Object principal = authentication.getPrincipal();

        // Run after updateAuthenticationPrincipal
        if (principal instanceof CustomUserDetails customUserDetails) {
            UUID userId = customUserDetails.getId();

            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage()));
        }

        if (principal instanceof OAuth2User oauthUser) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            String provider = token.getAuthorizedClientRegistrationId();
            String providerId = oauthUser.getName();
            AuthProvider authProviderEnum = AuthProvider.from(provider);

            User user = userRepository.findByProviderAndProviderId(authProviderEnum, providerId);

            if (user == null) {
                throw new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage());
            }

            updateAuthenticationPrincipal(user, authentication);

            return user;
        }

        if (authentication instanceof WebAuthnAuthentication) {
            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage()));

            updateAuthenticationPrincipal(user, authentication);

            return user;
        }

        throw new RuntimeException("Unsupported authentication type");
    }

    @Override
    public UserResDto getUserInfo(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        UserResDto response = new UserResDto();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setVerified(user.isVerified());

        return response;
    }

    @Override
    public List<UserResDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getLockedUntil(),
                        user.isVerified(),
                        AuthProvider.LOCAL.equals(user.getProvider())
                ))
                .toList();
    }

    @Override
    public boolean updateUserInfo(Authentication authentication, UserUpdateDto userDto, HttpServletRequest request) {
        User user = getUserFromAuthentication(authentication);

        user.setName(userDto.getName());
        user.setPhone(userDto.getPhone());

        boolean emailChanged = false;

        String addingEmail = userDto.getEmail();
        String currentEmail = user.getEmail();

        if (!addingEmail.equals(currentEmail)) {

            emailChanged = true;

            user.setVerified(false);
            var guestRole = roleRepository.findByName(RoleEnum.ROLE_GUEST)
                    .orElseThrow(() -> new RuntimeException(ErrorMessage.ROLE_NOT_FOUND.getMessage()));

            user.getRoles().clear();
            user.getRoles().add(guestRole);
            user.setEmail(addingEmail);

            tokenLoginService.sendUserTokenLink(addingEmail, UserTokenType.VERIFY);
        }
        if (emailChanged) {
            requestContextFingerprintService.buildSecurityEventContext(currentEmail, request, SecurityEventContext.CHANGE_EMAIL.getMessage());
        }

        User savedUser = userRepository.save(user);
        updateAuthenticationPrincipal(savedUser, authentication);

        return emailChanged;
    }

    @Override
    public UserResDto processPostLogin(Authentication authentication)
    {
        User user;

        if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {

            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

            String provider = token.getAuthorizedClientRegistrationId();
            String providerId = oauthUser.getName();

            AuthProvider authProviderEnum = AuthProvider.from(provider);

            user = userRepository.findByProviderAndProviderId(authProviderEnum, providerId);

            if (user == null) {
                String userEmail = oauthUser.getAttribute("email");
                String userName = oauthUser.getAttribute("name");
                user = new User();

                user.setEmail(userEmail);
                user.setName(userName);
                user.setProviderId(providerId);
                user.setProvider(AuthProvider.from(provider));
                user.setVerified(false);
                user.setSecret(TotpUtil.generateSecret());

                var role = roleRepository.findByName(RoleEnum.ROLE_GUEST)
                        .orElseThrow(() -> new RuntimeException(ErrorMessage.ROLE_NOT_FOUND.getMessage()));
                user.setRoles(Set.of(role));

                tokenLoginService.sendUserTokenLink(userEmail, UserTokenType.VERIFY);
                userRepository.save(user);
            }

        } else {
            user = getUserFromAuthentication(authentication);
        }

        UserResDto response = new UserResDto();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setVerified(user.isVerified());
        response.setLocalUser(user.getProvider() == AuthProvider.LOCAL);

        return response;
    }

    @Override
    public void changePassword(String email, ChangePasswordDto changePasswordDTO, HttpServletRequest request)
    {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage()));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new RuntimeException("OAuth2 users cannot change password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
        requestContextFingerprintService.buildSecurityEventContext(email, request, SecurityEventContext.CHANGE_PASSWORD.getMessage());
    }

    @Override
    public boolean checkCurrentPassword(String email, String currentPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage()));

        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    @Override
    public UUID getLoggedInUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        return null;
    }

    @Override
    public boolean isUserExist(String email){
        return userRepository.existsByEmail(email);
    }
}
