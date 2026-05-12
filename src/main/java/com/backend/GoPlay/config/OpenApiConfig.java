package com.backend.GoPlay.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GoPlay API Documentation",
                version = "1.0",
                description = "Tài liệu API cho dự án GoPlay. Cung cấp các endpoint để quản lý sân, đặt sân, thanh toán, và người dùng.",
                contact = @Contact(name = "GoPlay Developer", email = "developer@goplay.com")
        ),
        security = {
                // Áp dụng bảo mật cho tất cả các API mặc định
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Nhập Access Token (JWT) của bạn vào đây. Không cần thêm chữ 'Bearer ' ở đằng trước.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
