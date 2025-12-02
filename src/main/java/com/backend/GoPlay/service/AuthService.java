package com.backend.GoPlay.service;


import com.backend.GoPlay.dto.auth.AuthResponse;
import com.backend.GoPlay.dto.auth.LoginRequest;
import com.backend.GoPlay.dto.auth.RegisterRequest;
import com.backend.GoPlay.dto.auth.SocialLoginRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    /**
     * Xử lý luồng đăng nhập/đăng ký qua Google hoặc Facebook.
     * @param request Chứa ID Token hoặc Access Token từ mạng xã hội.
     * @return JWT nội bộ của ứng dụng.
     */
    AuthResponse socialLogin(SocialLoginRequest request);
}