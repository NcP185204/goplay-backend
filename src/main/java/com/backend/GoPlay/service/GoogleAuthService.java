package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.auth.SocialUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    /**
     * Xác minh Google ID Token và lấy thông tin User (OpenID Connect)
     */
    public SocialUserInfo verifyGoogleIdToken(String idTokenString) {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String name = (String) payload.get("name");

                return new SocialUserInfo(email, name, "GOOGLE");
            }
        } catch (Exception e) {
            System.err.println("Google Token Verification Failed: " + e.getMessage());
        }
        throw new SecurityException("Google ID Token không hợp lệ hoặc đã hết hạn.");
    }
}