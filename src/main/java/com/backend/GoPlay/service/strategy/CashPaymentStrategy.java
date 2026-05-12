package com.backend.GoPlay.service.strategy;

import com.backend.GoPlay.dto.payment.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public String getPaymentMethodName() {
        return "CASH";
    }

    @Override
    public PaymentResponse createPaymentRequest(String orderId, Double amount) {
        // Đối với tiền mặt, không có link thanh toán
        return PaymentResponse.builder()
                .message("Đơn hàng sẽ được thanh toán bằng tiền mặt tại sân.")
                .build();
    }
}
