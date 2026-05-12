package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.auth.SocialUserInfo;
import com.backend.GoPlay.service.strategy.SocialAuthStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FacebookAuthService implements SocialAuthStrategy {

    @Value("${facebook.app.id}")
    private String facebookAppId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getProviderName() {
        return "FACEBOOK";
    }

    @Override
    public SocialUserInfo verifyToken(String accessToken) {
        String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.get("id") == null) {
                throw new IllegalArgumentException("Invalid Facebook token.");
            }

            String email = (String) response.get("email");
            String name = (String) response.get("name");
            String pictureUrl = "";
            if (response.get("picture") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> pictureData = (Map<String, Object>) response.get("picture");
                if (pictureData.get("data") instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) pictureData.get("data");
                    pictureUrl = (String) data.get("url");
                }
            }

            return new SocialUserInfo(email, name, pictureUrl);
        } catch (Exception e) {
            throw new RuntimeException("Facebook token verification failed", e);
        }
    }
}
