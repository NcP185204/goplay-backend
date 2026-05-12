package com.backend.GoPlay.dto.payment;

import lombok.Data;

/**
 * DTO này dùng để hứng kết quả trả về từ API tạo đơn hàng của MoMo
 * (API: https://test-payment.momo.vn/v2/gateway/api/create)
 */
@Data
public class MomoCreatePaymentResponse {
    private String partnerCode;
    private String requestId;
    private String orderId;
    private Long amount;
    private Long responseTime;
    private String message;
    private Integer resultCode;  // 0 là thành công
    private String payUrl;       // URL để chuyển hướng người dùng đến trang quét mã QR
    private String deeplink;     // URL để mở thẳng app MoMo (nếu dùng trên điện thoại)
    private String qrCodeUrl;    // URL chứa ảnh QR
    private String deeplinkWebInApp; // Dùng cho web in-app MoMo
    private String signature;    // Chữ ký (có thể có hoặc không tùy phiên bản API)
}
