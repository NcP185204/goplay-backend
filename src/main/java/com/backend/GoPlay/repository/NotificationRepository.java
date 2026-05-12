package com.backend.GoPlay.repository;

import com.backend.GoPlay.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // Lấy danh sách thông báo của 1 user, sắp xếp mới nhất lên đầu
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    // Đếm số thông báo chưa đọc để hiển thị chấm đỏ
    long countByUserIdAndIsReadFalse(Integer userId);
}
