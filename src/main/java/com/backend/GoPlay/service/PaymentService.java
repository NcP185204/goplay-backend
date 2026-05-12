package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.payment.PaymentResponse;
import com.backend.GoPlay.model.User;

public interface PaymentService {
    // Tạo link thanh toán cho một đơn hàng
    PaymentResponse createPayment(Integer bookingId, User user);

    // Xử lý khi thanh toán thành công (Momo gọi về webhook)
    void handleSuccessfulPayment(String transactionId);

    // Xử lý khi thanh toán thất bại/hủy (Momo gọi về webhook)
    void handleFailedPayment(String transactionId, String errorMessage);

    // Lấy thông tin thanh toán hiện tại
    PaymentResponse getPaymentInfo(Integer bookingId, User user);
}
