package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.auth.*;
import com.backend.GoPlay.model.RefreshToken;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.UserRepository;
import com.backend.GoPlay.security.JwtTokenProvider;
import com.backend.GoPlay.service.AuthService;
import com.backend.GoPlay.service.RefreshTokenService;
import com.backend.GoPlay.service.strategy.SocialAuthStrategy;
import com.backend.GoPlay.service.strategy.SocialAuthStrategyFactory;
import com.backend.GoPlay.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final SocialAuthStrategyFactory socialAuthStrategyFactory; // Inject Factory
    private final ConversionService conversionService; // Inject Spring ConversionService

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }
        
        // Dùng ConversionService để chuyển đổi DTO -> Entity
        User user = conversionService.convert(request, User.class);
        if (user == null) {
             throw new IllegalArgumentException("Lỗi chuyển đổi dữ liệu đăng ký");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Set password mã hóa
        
        User savedUser = userRepository.save(user);

        return generateAndBuildAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        return generateAndBuildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request) {
        // Lấy đúng strategy từ factory
        SocialAuthStrategy strategy = socialAuthStrategyFactory.getStrategy(request.getProvider());
        // Thực thi strategy
        SocialUserInfo socialUserInfo = strategy.verifyToken(request.getToken());

        User user = userRepository.findByEmail(socialUserInfo.getEmail()).orElseGet(() -> {
            // KHÔNG DÙNG ConversionService Ở ĐÂY NỮA VÌ CHƯA CẤU HÌNH CONVERTER
            // Tạo User bằng tay
            User newUser = User.builder()
                    .email(socialUserInfo.getEmail())
                    .fullName(socialUserInfo.getName())
                    // Cấp quyền mặc định cho user đăng nhập qua mạng xã hội
                    .role(UserRole.PLAYER) 
                    .password(passwordEncoder.encode("SOCIAL_USER_PASSWORD_" + System.currentTimeMillis()))
                    .build();
            return userRepository.save(newUser);
        });

        return generateAndBuildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    private AuthResponse generateAndBuildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }
}
