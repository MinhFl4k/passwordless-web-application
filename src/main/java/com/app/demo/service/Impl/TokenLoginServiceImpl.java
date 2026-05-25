package com.app.demo.service.Impl;

import com.app.demo.dto.common.CustomUserDetails;
import com.app.demo.dto.response.UserVerifiedResDto;
import com.app.demo.enums.ErrorMessage;
import com.app.demo.enums.RoleEnum;
import com.app.demo.enums.TokenStatus;
import com.app.demo.enums.UserTokenType;
import com.app.demo.model.UserToken;
import com.app.demo.model.User;
import com.app.demo.repository.RoleRepository;
import com.app.demo.repository.UserTokenRepository;
import com.app.demo.repository.UserRepository;
import com.app.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenLoginServiceImpl implements UserTokenService {

    private final JwtService jwtService;

    private final EmailService emailService;

    private final UserDetailsService userDetailsService;

    private final RequestContextFingerprintService requestContextFingerprintService;

    private final UserRepository userRepository;

    private final UserTokenRepository userTokenRepository;

    private final RoleRepository roleRepository;

    private final UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    @Value("${send.code.token.timeout}")
    private long SEND_TOKEN_TIMEOUT;

    @Value("${app.full.url}")
    private String APP_FULL_URL;

    @Override
    @Transactional
    public void sendUserTokenLink(String email, UserTokenType userTokenType) {

        if (email.isEmpty())
        {
            throw new RuntimeException(ErrorMessage.EMAIL_REQUIRED.getMessage());
        }

        String tokenType = userTokenType.getType();

        Optional<UserToken> lastCreatedToken = userTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDesc(email, tokenType);

        if (lastCreatedToken.isPresent()) {
            UserToken lastToken = lastCreatedToken.get();
            LocalDateTime allowedTime = lastToken.getCreatedAt()
                    .plusSeconds(SEND_TOKEN_TIMEOUT);

            if (LocalDateTime.now().isBefore(allowedTime)) {
                throw new RuntimeException("Please wait " + SEND_TOKEN_TIMEOUT + " seconds before sending the link again");
            }
        }

        userTokenRepository.expireAllOldToken(email, tokenType);

        String token = jwtService.generateToken(email);
        String tokenHash = requestContextFingerprintService.sha256(token);

        String link = APP_FULL_URL + userTokenType.getPath() + token;

        UserToken userToken = new UserToken();
        userToken.setEmail(email);
        userToken.setToken(tokenHash);
        userToken.setCreatedAt(LocalDateTime.now());
        userToken.setType(tokenType);
        userToken.setExpiryTime(LocalDateTime.now().plusMinutes(userTokenType.getTimeout()));
        userToken.setUsed(false);

        userTokenRepository.save(userToken);

        emailService.sendUserTokenLink(email, link, userTokenType);
    }

    @Override
    public UserVerifiedResDto verifyWithUserToken(String token, HttpServletRequest request) {
        CustomUserDetails userDetails = validateUserToken(token);

        User user = userDetails.getUser();
        if (user == null) {
            throw new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage());
        }

        var userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.ROLE_NOT_FOUND.getMessage()));

        user.getRoles().clear();
        user.getRoles().add(userRole);

        user.setVerified(true);
        userRepository.save(user);

        UserVerifiedResDto userVerifiedResDto = new UserVerifiedResDto();
        userVerifiedResDto.setName(user.getName());
        userVerifiedResDto.setEmail(user.getEmail());

        return userVerifiedResDto;
    }

    @Override
    public CustomUserDetails validateUserToken(String token) {
        if (token == null || !jwtService.isValid(token)) {
            throw new RuntimeException(TokenStatus.INVALID.getMessage());
        }

        String tokenHash = requestContextFingerprintService.sha256(token);

        UserToken userToken = userTokenRepository
                .findByToken(tokenHash)
                .orElseThrow(() -> new RuntimeException(TokenStatus.NOT_FOUND.getMessage()));

        if (userToken.isUsed() || userToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(TokenStatus.EXPIRED.getMessage());
        }

        String email = jwtService.extractEmail(token);

        CustomUserDetails userDetails;
        userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        userDetailsChecker.check(userDetails);

        userToken.setUsed(true);
        userTokenRepository.save(userToken);

        return userDetails;
    }
}
