package com.app.demo.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface SessionSecurityService {
    void initializeSnapshot(String email, HttpServletRequest request, HttpSession session);

    boolean isContextChanged(String email, HttpServletRequest request, HttpSession session);
}
