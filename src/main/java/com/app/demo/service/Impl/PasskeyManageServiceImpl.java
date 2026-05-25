package com.app.demo.service.Impl;

import com.app.demo.dto.response.PasskeyResDto;
import com.app.demo.service.PasskeyManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.app.demo.util.DateTimeFormatUtil.DATETIME_FORMATTER;

@Service
@RequiredArgsConstructor
public class PasskeyManageServiceImpl implements PasskeyManageService {

    private final JdbcPublicKeyCredentialUserEntityRepository userEntityRepository;
    private final JdbcUserCredentialRepository userCredentialRepository;


    @Override
    public List<PasskeyResDto> findPasskeysByUsername(String username) {
        PublicKeyCredentialUserEntity userEntity = this.userEntityRepository.findByUsername(username);
        if (userEntity == null) {
            return Collections.emptyList();
        }

        List<CredentialRecord> records = this.userCredentialRepository.findByUserId(userEntity.getId());

        return records.stream()
                .map(record -> {
                    record.getCreated();
                    record.getLastUsed();
                    return new PasskeyResDto(
                            record.getCredentialId().toBase64UrlString(),
                            record.getLabel(),
                            DATETIME_FORMATTER.format(record.getCreated()),
                            DATETIME_FORMATTER.format(record.getLastUsed())
                    );
                })
                .toList();
    }

    @Override
    public void deletePasskeyForUser(String username, String credentialIdBase64Url) {
        PublicKeyCredentialUserEntity userEntity = this.userEntityRepository.findByUsername(username);
        if (userEntity == null) {
            throw new IllegalArgumentException("User does not have any passkeys");
        }

        List<CredentialRecord> records = this.userCredentialRepository.findByUserId(userEntity.getId());

        CredentialRecord matched = records.stream()
                .filter(r -> r.getCredentialId().toBase64UrlString().equals(credentialIdBase64Url))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Credential does not belong to current user"));

        this.userCredentialRepository.delete(matched.getCredentialId());
    }
}
