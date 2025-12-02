package com.backend.GoPlay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quan hệ: Một sân (Court) có nhiều khung giờ (TimeSlot)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean isAvailable;

    // Sửa từ đây: Thêm quan hệ với User (người đã đặt)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "player_id") // Khóa ngoại (Foreign Key) trỏ đến bảng users
//    private User player; // Đây là người chơi (User) đã đặt khung giờ này
//
//    // Sau này, khi được đặt, bạn sẽ thêm @ManyToOne User (người đã đặt) vào đây
}