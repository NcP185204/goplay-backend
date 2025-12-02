package com.backend.GoPlay.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SocialUserInfo {
    private String email;
    private String name;
    private String provider;
}