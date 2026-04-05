package com.logarithm.microtask.service;

import com.logarithm.microtask.dto.auth.AuthResponse;
import com.logarithm.microtask.dto.auth.LoginRequest;
import com.logarithm.microtask.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);
}
