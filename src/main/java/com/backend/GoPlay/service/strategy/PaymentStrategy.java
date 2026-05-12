package com.backend.GoPlay.service.strategy;

import com.backend.GoPlay.dto.payment.PaymentResponse;

public interface PaymentStrategy {
    String getPaymentMethodName();
    PaymentResponse createPaymentRequest(String orderId, Double amount);
}
