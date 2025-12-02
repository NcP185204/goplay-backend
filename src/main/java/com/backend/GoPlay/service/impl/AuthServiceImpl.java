package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.auth.*;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.UserRepository;
import com.backend.GoPlay.security.JwtTokenProvider;
import com.backend.GoPlay.service.AuthService;
import com.backend.GoPlay.service.FacebookAuthService;
import com.backend.GoPlay.service.GoogleAuthService;
import com.backend.GoPlay.util.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final GoogleAuthService googleAuthService; // <-- INJECT
    private final FacebookAuthService facebookAuthService; // <-- INJECT

    @Override
    public AuthResponse register(RegisterRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }

        // 2. Tạo User mới và mã hóa mật khẩu
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                // .role(UserRole.PLAYER) // Tạm thời bỏ dòng này
                .build();

        // GÁN VAI TRÒ MỘT CÁCH TƯỜNG MINH ĐỂ ĐẢM BẢO AN TOÀN
        user.setRole(UserRole.PLAYER);

        // 3. Lưu vào CSDL
        User savedUser = userRepository.save(user);

        // 4. Tạo token
        String jwtToken = jwtTokenProvider.generateToken(savedUser);

        // 5. Trả về response
        return AuthResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực bằng Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Nếu xác thực thành công, set vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Lấy thông tin User đã được xác thực
        User user = (User) authentication.getPrincipal();

        // 4. Tạo token
        String jwtToken = jwtTokenProvider.generateToken(user);

        // 5. Trả về response
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request) {

        // 1. XÁC MINH TOKEN VÀ LẤY THÔNG TIN USER
        SocialUserInfo socialUserInfo;

        if ("GOOGLE".equalsIgnoreCase(request.getProvider())) {
            socialUserInfo = googleAuthService.verifyGoogleIdToken(request.getToken());
        }
        else if ("FACEBOOK".equalsIgnoreCase(request.getProvider())) {
            socialUserInfo = facebookAuthService.verifyFacebookToken(request.getToken());
        }
        else {
            throw new IllegalArgumentException("Nhà cung cấp không được hỗ trợ.");
        }

        // 2. TÌM HOẶC TẠO USER TRONG DATABASE NỘI BỘ (Provisioning)
        Optional<User> existingUser = userRepository.findByEmail(socialUserInfo.getEmail());
        User user;

        if (existingUser.isEmpty()) {
            // User mới: Tạo user mới với mật khẩu giả và Role PLAYER
            user = User.builder()
                    .email(socialUserInfo.getEmail())
                    .fullName(socialUserInfo.getName())
                    .password(passwordEncoder.encode("SOCIAL_USER_PASSWORD")) // Mật khẩu placeholder
                    .role(UserRole.PLAYER)
                    .build();
            user = userRepository.save(user);
        } else {
            user = existingUser.get();
            // Đảm bảo user cũ cũng có vai trò
            if (user.getRole() == null) {
                user.setRole(UserRole.PLAYER);
                user = userRepository.save(user);
            }
        }

        // 3. TẠO VÀ TRẢ VỀ JWT NỘI BỘ (Chìa khóa của ứng dụng GoPlay)
        String jwtToken = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
