package com.backend.GoPlay.controller;

import com.backend.GoPlay.dto.auth.AuthResponse;
import com.backend.GoPlay.dto.auth.LoginRequest;
import com.backend.GoPlay.dto.auth.RegisterRequest;
import com.backend.GoPlay.dto.auth.SocialLoginRequest;
import com.backend.GoPlay.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // API chung cho xác thực
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        // Gọi service để xử lý
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        // Gọi service để xử lý
        return ResponseEntity.ok(authService.login(request));
    }
    /**
     * API Đăng nhập/Đăng ký qua mạng xã hội (Hoàn tất)
     */
    @PostMapping("/social-login")
    public ResponseEntity<AuthResponse> socialLogin(
            @RequestBody SocialLoginRequest request
    ) {
        AuthResponse response = authService.socialLogin(request);
        return ResponseEntity.ok(response);
    }
}