package com.backend.GoPlay.dto.booking;

import com.backend.GoPlay.util.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private Integer id;
    private String courtName; // Tên sân
    private String courtAddress;
    private Double latitude;  // Thêm vĩ độ để dẫn đường
    private Double longitude; // Thêm kinh độ để dẫn đường
    private Double totalPrice;
    private BookingStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private List<String> timeSlotDetails; // Danh sách các khung giờ đã đặt
}
