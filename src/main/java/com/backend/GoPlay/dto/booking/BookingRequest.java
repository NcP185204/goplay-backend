package com.backend.GoPlay.dto.booking;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data // Annotation này sẽ tự động tạo Getter, Setter, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotEmpty(message = "Danh sách ID khung giờ không được để trống")
    private List<Integer> timeSlotIds = new ArrayList<>();

    private String note;
    
    // Thêm trường chọn phương thức thanh toán. Mặc định là MOMO nếu client không gửi.
    private String paymentMethod = "MOMO"; 
}
