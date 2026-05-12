package com.backend.GoPlay.service.strategy;

import com.backend.GoPlay.dto.auth.SocialUserInfo;

public interface SocialAuthStrategy {
    /**
     * Trả về tên của provider mà strategy này hỗ trợ (ví dụ: "GOOGLE", "FACEBOOK").
     */
    String getProviderName();

    /**
     * Xác thực token và trả về thông tin người dùng.
     */
    SocialUserInfo verifyToken(String token);
}
