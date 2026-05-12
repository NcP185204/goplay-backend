package com.backend.GoPlay.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String paymentUrl; // URL để hiển thị mã QR
    private String message;
}
    