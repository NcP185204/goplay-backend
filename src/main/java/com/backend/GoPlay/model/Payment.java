package com.backend.GoPlay.model;

import com.backend.GoPlay.util.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 1. Liên kết Một-Một với Booking
    // Một Payment chỉ thuộc về một Booking duy nhất
    @OneToOne
    @JoinColumn(name = "booking_id", referencedColumnName = "id", nullable = false)
    private Booking booking;

    // 2. Ai là người thanh toán?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 3. Số tiền
    @Column(nullable = false)
    private Double amount;

    // 4. Trạng thái thanh toán
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // 5. Phương thức thanh toán (sau này có thể mở rộng)
    private String paymentMethod; // Ví dụ: "MOMO", "VNPAY", "CASH"

    // 6. ID giao dịch từ cổng thanh toán
    @Column(unique = true)
    private String transactionId; // Tương đương paymentId ở thiết kế cũ

    // 7. Ngày tạo
    
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 8. Ngày cập nhật
    private LocalDateTime updatedAt;
}