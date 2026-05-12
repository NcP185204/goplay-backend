package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.event.BookingConfirmedEvent;
import com.backend.GoPlay.model.Booking;
import com.backend.GoPlay.model.Notification;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.NotificationRepository;
import com.backend.GoPlay.service.NotificationService;
import com.backend.GoPlay.util.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final FirebasePushNotificationService pushNotificationService; // Sẽ thêm ở bước sau

    @EventListener
    @Transactional
    @Override
    public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
        // 1. Lấy thông tin cần thiết từ sự kiện
        Booking booking = event.getBooking();
        User user = booking.getUser();

        // Tên sân (lấy từ timeSlot đầu tiên của booking)
        String courtName = booking.getTimeSlots().get(0).getCourt().getName();

        // 2. Soạn nội dung thông báo
        String title = "Đặt sân thành công!";
        String content = "Tuyệt vời! Bạn đã đặt thành công sân " + courtName + ". Mã đơn: " + booking.getId();

        // 3. Tạo đối tượng Notification Entity
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(NotificationType.BOOKING_SUCCESS)
                .relatedId(booking.getId()) // Lưu ID đơn hàng để sau này App biết mở màn hình nào
                .isRead(false)
                .build();

        // 4. Lưu vào Database
        notificationRepository.save(notification);
        log.info("Đã lưu thông báo vào Database cho user: {}", user.getEmail());

//         5. Gửi Push Notification qua Firebase (Sẽ làm ở bước 3)
         if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
             pushNotificationService.sendPushNotification(user.getFcmToken(), title, content);
         }
    }
}