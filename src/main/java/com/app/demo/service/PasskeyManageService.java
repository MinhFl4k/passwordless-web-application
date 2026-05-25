package com.app.demo.service;

import com.app.demo.dto.response.PasskeyResDto;

import java.util.List;

public interface PasskeyManageService {

    List<PasskeyResDto> findPasskeysByUsername(String username);

    void deletePasskeyForUser(String username, String credentialIdBase64Url);
}
