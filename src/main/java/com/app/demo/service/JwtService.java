package com.app.demo.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface JwtService {

    String generateToken(String email);

    String extractEmail(String token);

    boolean isValid(String token);

    Jws<Claims> parse(String token);
}
