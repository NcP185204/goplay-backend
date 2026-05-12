package com.backend.GoPlay.service.strategy;

import com.backend.GoPlay.dto.payment.MomoCreatePaymentRequest;
import com.backend.GoPlay.dto.payment.MomoCreatePaymentResponse;
import com.backend.GoPlay.dto.payment.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.Base64;

@Component
public class MomoPaymentStrategy implements PaymentStrategy {

    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;

    @Value("${momo.access-key:ACCESS_KEY}")
    private String accessKey;

    @Value("${momo.secret-key:SECRET_KEY}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;

    @Value("${momo.return-url:http://localhost:8080/api/payments/momo/return}")
    private String returnUrl;

    @Value("${momo.notify-url:http://localhost:8080/api/payments/momo/success}")
    private String notifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getPaymentMethodName() {
        return "MOMO";
    }

    @Override
    public PaymentResponse createPaymentRequest(String orderId, Double amount) {
        try {
            Long amountLong = amount.longValue();
            String amountStr = String.valueOf(amountLong);
            String requestId = UUID.randomUUID().toString();
            String orderInfo = "Thanh toán đặt sân GoPlay";
            String requestType = "captureWallet";
            
            // MoMo khuyên dùng Base64 rỗng hoặc chuỗi rỗng cho extraData nếu không có
            String extraData = ""; 

            // 1. Tạo chuỗi dữ liệu (raw hash)
            // LƯU Ý QUAN TRỌNG: Thứ tự các key PHẢI theo đúng bảng chữ cái (Alphabetical Order)
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + amountStr +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            // 2. Tạo chữ ký
            String signature = hmacSHA256(rawHash, secretKey);

            // 3. Dùng DTO để tạo Request Body
            MomoCreatePaymentRequest requestBody = MomoCreatePaymentRequest.builder()
                    .partnerCode(partnerCode)
                    .partnerName("Test")
                    .storeId("MomoTestStore")
                    .requestType(requestType)
                    .ipnUrl(notifyUrl)
                    .redirectUrl(returnUrl)
                    .orderId(orderId)
                    .amount(amountLong) // Truyền Long thay vì String
                    .lang("vi")
                    .orderInfo(orderInfo)
                    .requestId(requestId)
                    .extraData(extraData)
                    .signature(signature)
                    .build();

            // 4. Cấu hình Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MomoCreatePaymentRequest> entity = new HttpEntity<>(requestBody, headers);

            // 5. Gửi POST Request và hứng dữ liệu bằng DTO Response
            ResponseEntity<MomoCreatePaymentResponse> response = restTemplate.postForEntity(
                    endpoint,
                    entity,
                    MomoCreatePaymentResponse.class
            );

            MomoCreatePaymentResponse responseBody = response.getBody();

            // 6. Xử lý kết quả
            if (responseBody != null && responseBody.getResultCode() != null) {
                if (responseBody.getResultCode() == 0) { // Thành công
                    return PaymentResponse.builder()
                            .paymentUrl(responseBody.getPayUrl())
                            .message("Tạo link thanh toán Momo thành công.")
                            .build();
                } else {
                    System.err.println("Lỗi từ MoMo: " + responseBody.getMessage() + " | Raw Response: " + responseBody);
                    throw new RuntimeException("Lỗi từ MoMo: " + responseBody.getMessage());
                }
            }
            throw new RuntimeException("Không nhận được phản hồi hợp lệ từ MoMo");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo yêu cầu thanh toán MoMo: " + e.getMessage());
        }
    }

    // Hàm tiện ích để mã hóa HMAC SHA256 (Chuẩn của MoMo)
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
