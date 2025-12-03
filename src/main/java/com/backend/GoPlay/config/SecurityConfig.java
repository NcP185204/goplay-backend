package com.backend.GoPlay.config; // Đảm bảo đúng package của bạn

import com.backend.GoPlay.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Bật tính năng @PreAuthorize, @Secured
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService; // Đã được implement bởi UserDetailsServiceImpl

    // Danh sách các đường dẫn không cần xác thực
    private static final String[] WHITE_LIST_URLS = {
            "/api/auth/**",           // Cho phép API đăng ký, đăng nhập
            "/v3/api-docs/**",        // Cho phép Swagger/OpenAPI
            "/swagger-ui/**",         // Cho phép Swagger UI
            "/h2-console/**"          // (Nếu dùng) Cho phép H2 Console
            // Thêm các API public khác nếu có (ví dụ: /api/courts/search)
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì chúng ta dùng JWT (stateless)
                .csrf(csrf -> csrf.disable()) // Tắt tính năng bảo vệ CSRF

                // Cấu hình các đường dẫn được phép
                .authorizeHttpRequests(req -> req
                        .requestMatchers(WHITE_LIST_URLS).permitAll() // Cho phép các đường dẫn trong WHITE_LIST
                        .anyRequest().authenticated() // Tất cả các request còn lại phải được xác thực
                )
                // Không dùng session (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Cung cấp AuthenticationProvider
                .authenticationProvider(authenticationProvider())
                // Thêm filter JWT vào trước filter UsernamePassword
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // (Nếu dùng H2) Cho phép H2 Console hiển thị trong frame
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    // Bean cung cấp PasswordEncoder (dùng BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean cung cấp AuthenticationProvider
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Cung cấp cách tìm User
        authProvider.setPasswordEncoder(passwordEncoder()); // Cung cấp cách check Pass
        return authProvider;
    }

    // Bean cung cấp AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}