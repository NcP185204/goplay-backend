package com.backend.GoPlay.util;

public enum PaymentStatus {
    PENDING,    // Đang chờ thanh toán
    SUCCESS,    // Thanh toán thành công
    FAILED,     // Thanh toán thất bại
    EXPIRED     // Hết hạn thanh toán
}
