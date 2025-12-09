package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.auth.*;
import com.backend.GoPlay.model.RefreshToken;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.UserRepository;
import com.backend.GoPlay.security.JwtTokenProvider;
import com.backend.GoPlay.service.AuthService;
import com.backend.GoPlay.service.FacebookAuthService;
import com.backend.GoPlay.service.GoogleAuthService;
import com.backend.GoPlay.service.RefreshTokenService;
import com.backend.GoPlay.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final GoogleAuthService googleAuthService;
    private final FacebookAuthService facebookAuthService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.PLAYER)
                .build();
        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getEmail());

        return buildAuthResponse(savedUser, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request) {
        SocialUserInfo socialUserInfo;
        if ("GOOGLE".equalsIgnoreCase(request.getProvider())) {
            socialUserInfo = googleAuthService.verifyGoogleIdToken(request.getToken());
        } else if ("FACEBOOK".equalsIgnoreCase(request.getProvider())) {
            socialUserInfo = facebookAuthService.verifyFacebookToken(request.getToken());
        } else {
            throw new IllegalArgumentException("Nhà cung cấp không được hỗ trợ.");
        }

        User user = userRepository.findByEmail(socialUserInfo.getEmail()).orElseGet(() -> {
            User newUser = User.builder()
                    .email(socialUserInfo.getEmail())
                    .fullName(socialUserInfo.getName())
                    .password(passwordEncoder.encode("SOCIAL_USER_PASSWORD"))
                    .role(UserRole.PLAYER)
                    .build();
            return userRepository.save(newUser);
        });

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
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
}
