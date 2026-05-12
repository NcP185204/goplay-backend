package com.backend.GoPlay.controller;

import com.backend.GoPlay.dto.notification.NotificationResponse;
import com.backend.GoPlay.model.Notification;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // 1. Lấy danh sách thông báo của tôi (Frontend dùng để vẽ List)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Lấy Page<Notification> từ DB
        Page<Notification> notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageRequest);

        // Dùng hàm map() của Page để chuyển đổi từng Notification sang NotificationResponse
        Page<NotificationResponse> responsePage = notificationPage.map(this::mapToResponse);

        return ResponseEntity.ok(responsePage);
    }

    // 2. Đếm số thông báo chưa đọc (Frontend dùng để vẽ cái chấm đỏ)
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationRepository.countByUserIdAndIsReadFalse(user.getId()));
    }

    // 3. Đánh dấu đã đọc 1 thông báo
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id, @AuthenticationPrincipal User user) {
        notificationRepository.findById(id).ifPresent(notification -> {
            // Kiểm tra bảo mật: Chỉ cho phép đọc thông báo của chính mình
            if (notification.getUser().getId().equals(user.getId())) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
        return ResponseEntity.ok().build();
    }

    // 4. Đánh dấu đã đọc TẤT CẢ thông báo
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        // Lấy tất cả thông báo chưa đọc của user này
        List<Notification> unreadNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), Pageable.unpaged())
                .stream()
                .filter(n -> !n.isRead())
                .toList();
                
        // Đổi trạng thái thành true
        unreadNotifications.forEach(n -> n.setRead(true));
        
        // Lưu hàng loạt
        notificationRepository.saveAll(unreadNotifications);
        
        return ResponseEntity.ok().build();
    }

    // Hàm helper để chuyển đổi từ Entity sang DTO
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .relatedId(notification.getRelatedId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
