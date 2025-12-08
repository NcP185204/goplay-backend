package com.backend.GoPlay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Khi một request có URL khớp với "/uploads/**"
        // Spring sẽ tìm file tương ứng trong thư mục "uploads/" trên hệ thống file.
        // Ví dụ: request đến "/uploads/courts/abc.jpg" sẽ được map tới file "uploads/courts/abc.jpg".
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
