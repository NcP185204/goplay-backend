package com.backend.GoPlay.dto.payment;

import lombok.Data;

/**
 * DTO này dùng để hứng dữ liệu từ Webhook (IPN - Instant Payment Notification)
 * do MoMo gửi về server của chúng ta sau khi người dùng thanh toán xong.
 */
@Data
public class MomoIpnRequest {
    private String partnerCode;
    private String orderId;     // Đây chính là transactionId mà chúng ta gửi cho MoMo
    private String requestId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private Long transId;       // Mã giao dịch thực tế trên hệ thống MoMo
    private Integer resultCode; // resultCode = 0 nghĩa là thanh toán thành công
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;
    private String signature;   // Dùng để xác minh tính toàn vẹn của dữ liệu
}

