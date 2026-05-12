package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.UserRepository;
import com.backend.GoPlay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void updateFcmToken(User user, String fcmToken) {
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }
}
