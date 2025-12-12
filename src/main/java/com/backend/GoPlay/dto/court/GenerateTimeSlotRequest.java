package com.backend.GoPlay.dto.court;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class GenerateTimeSlotRequest {

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @Min(value = 1, message = "Số ngày phải lớn hơn 0")
    private int numberOfDays = 7; // Mặc định là 7 ngày

    @NotNull(message = "Giờ mở cửa không được để trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @NotNull(message = "Giờ đóng cửa không được để trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    @Min(value = 30, message = "Thời lượng mỗi khung giờ phải ít nhất 30 phút")
    private int slotDurationInMinutes = 60; // Mặc định là 60 phút
}
