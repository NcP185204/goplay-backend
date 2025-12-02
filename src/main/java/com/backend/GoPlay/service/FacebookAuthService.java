package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.auth.SocialUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FacebookAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FacebookAuthService.class);

    // Khai báo RestTemplate (cần bean nếu dùng Spring Boot < 3.x)
    private final RestTemplate restTemplate = new RestTemplate();

    // Endpoint của Facebook Graph API để lấy tên và email
    private final String GRAPH_API_URL = "https://graph.facebook.com/me?fields=id,name,email&access_token={token}";

    /**
     * Xác minh Facebook Access Token và lấy thông tin user
     */
    public SocialUserInfo verifyFacebookToken(String accessToken) {

        try {
            // Gọi Facebook Graph API để lấy thông tin
            Map<String, Object> userData = restTemplate.getForObject(
                    GRAPH_API_URL,
                    (Class<Map<String, Object>>) (Class<?>) Map.class,
                    accessToken
            );

            // Phải có email để tạo tài khoản nội bộ
            if (userData != null && userData.containsKey("email")) {
                String email = (String) userData.get("email");
                String name = (String) userData.get("name");

                return new SocialUserInfo(email, name, "FACEBOOK");
            }
            throw new SecurityException("Facebook token hợp lệ nhưng không cung cấp quyền truy cập email.");

        } catch (HttpClientErrorException e) {
            // Ghi lại chi tiết lỗi từ Facebook (bao gồm cả lỗi 403)
            logger.error("Lỗi khi xác thực token Facebook. Status: {}", e.getStatusCode());
            logger.error("Response Body: {}", e.getResponseBodyAsString());
            throw new SecurityException("Facebook Access Token không hợp lệ hoặc đã hết hạn.");
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi xác thực token Facebook: {}", e.getMessage(), e);
            throw new SecurityException("Facebook Access Token không hợp lệ hoặc đã hết hạn.");
        }
    }
}
