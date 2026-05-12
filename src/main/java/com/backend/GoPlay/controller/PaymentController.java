package com.backend.GoPlay.controller;

import com.backend.GoPlay.dto.payment.MomoIpnRequest;
import com.backend.GoPlay.dto.payment.PaymentResponse;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @PostMapping("/bookings/{bookingId}/create-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Integer bookingId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.createPayment(bookingId, user));
    }

    @GetMapping("/bookings/{bookingId}/payment-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getPaymentInfo(
            @PathVariable Integer bookingId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.getPaymentInfo(bookingId, user));
    }

    // =========================================================================
    // API Webhook - MoMo gọi vào đây qua Ngrok
    // =========================================================================
    @PostMapping("/payments/momo/success")
    public ResponseEntity<Void> momoSuccessWebhook(@RequestBody MomoIpnRequest payload) {
        System.out.println("\n--- NHẬN ĐƯỢC WEBHOOK TỪ MOMO ---");
        System.out.println("OrderId: " + payload.getOrderId());
        System.out.println("ResultCode: " + payload.getResultCode());

        try {
            // 1. Kiểm tra tính toàn vẹn của dữ liệu (Signature Validation)
            // Phải ráp chuỗi theo đúng thứ tự tài liệu MoMo yêu cầu
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + payload.getAmount() +
                    "&extraData=" + payload.getExtraData() +
                    "&message=" + payload.getMessage() +
                    "&orderId=" + payload.getOrderId() +
                    "&orderInfo=" + payload.getOrderInfo() +
                    "&orderType=" + payload.getOrderType() +
                    "&partnerCode=" + payload.getPartnerCode() +
                    "&payType=" + payload.getPayType() +
                    "&requestId=" + payload.getRequestId() +
                    "&responseTime=" + payload.getResponseTime() +
                    "&resultCode=" + payload.getResultCode() +
                    "&transId=" + payload.getTransId();

            String expectedSignature = hmacSHA256(rawHash, secretKey);

            if (!expectedSignature.equals(payload.getSignature())) {
                System.err.println("❌ LỖI BẢO MẬT: Chữ ký không khớp! Có thể là request giả mạo.");
                // Trả về 200 OK nhưng không làm gì cả để hacker không biết là bị bắt bài
                return ResponseEntity.ok().build(); 
            }

            System.out.println("✅ Chữ ký hợp lệ. Request chính chủ từ MoMo.");

            String transactionId = payload.getOrderId(); // Mã giao dịch của mình

            // 2. Xử lý nghiệp vụ dựa trên ResultCode
            if (payload.getResultCode() == 0) {
                System.out.println("✅ Thanh toán THÀNH CÔNG cho mã: " + transactionId);
                // Gọi Service cập nhật Booking -> CONFIRMED, Payment -> SUCCESS
                paymentService.handleSuccessfulPayment(transactionId);
            } else {
                System.out.println("⚠️ Thanh toán THẤT BẠI. Lỗi: " + payload.getMessage());
                // Gọi Service cập nhật Booking -> CANCELLED, Payment -> FAILED và NHẢ SÂN LẠI
                paymentService.handleFailedPayment(transactionId, payload.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý Webhook MoMo: " + e.getMessage());
        }

        // Luôn trả về 200 OK để MoMo biết là server mình đã nhận được
        return ResponseEntity.ok().build();
    }

    // Hàm mã hóa chữ ký
    private String hmacSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
