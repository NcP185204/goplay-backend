package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.auth.AuthResponse;
import com.backend.GoPlay.dto.auth.LoginRequest;
import com.backend.GoPlay.dto.auth.RegisterRequest;
import com.backend.GoPlay.dto.auth.SocialLoginRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse socialLogin(SocialLoginRequest request);
}
