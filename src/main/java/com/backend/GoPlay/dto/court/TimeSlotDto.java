package com.backend.GoPlay.dto.court;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeSlotDto {

    // ID của khung giờ
    private String id;

    // ID của sân (để tiện cho frontend)
    private String courtId;

    // Thời điểm bắt đầu (dùng Long - Timestamp tính bằng milliseconds)
    private long startTime;

    // Thời điểm kết thúc (dùng Long - Timestamp tính bằng milliseconds)
    private long endTime;

    // Trạng thái khả dụng
    private boolean isAvailable;
}