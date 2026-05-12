package com.backend.GoPlay.dto.payment;

import lombok.Builder;
import lombok.Data;

/**
 * DTO này dùng để đóng gói dữ liệu gửi ĐẾN API tạo đơn hàng của MoMo.
 */
@Data
@Builder
public class MomoCreatePaymentRequest {
    private String partnerCode;
    private String partnerName;
    private String storeId;
    private String requestType;
    private String ipnUrl;
    private String redirectUrl;
    private String orderId;
    private Long amount; // MoMo sample request cho thấy amount là kiểu Long (không có ngoặc kép)
    private String lang;
    private String orderInfo;
    private String requestId;
    private String extraData;
    private String signature;
}
