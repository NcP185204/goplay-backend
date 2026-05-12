package com.backend.GoPlay.service;

import com.backend.GoPlay.model.User;

public interface UserService {
    // Lưu token Firebase Cloud Messaging của thiết bị
    void updateFcmToken(User user, String fcmToken);
}
