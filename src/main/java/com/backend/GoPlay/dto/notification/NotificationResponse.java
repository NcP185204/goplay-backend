package com.backend.GoPlay.dto.notification;

import com.backend.GoPlay.util.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Integer id;
    private String title;
    private String content;
    private NotificationType type;
    private Integer relatedId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
