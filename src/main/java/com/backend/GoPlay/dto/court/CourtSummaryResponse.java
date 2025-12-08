package com.backend.GoPlay.dto.court;

import com.backend.GoPlay.util.SportType;
import lombok.Builder;
import lombok.Data;

/**
 * DTO "nhẹ" dùng để hiển thị trong danh sách tìm kiếm hoặc trang chủ.
 * Chỉ chứa các thông tin tóm tắt và ảnh đại diện (thumbnail).
 */
@Data
@Builder
public class CourtSummaryResponse {
    private Integer id;
    private String name;
    private String address;
    private SportType courtType;
    private Double pricePerHour;
    private Double averageRating;
    private String thumbnailUrl; // Chỉ 1 ảnh đại diện
    private Double latitude;
    private Double longitude;
    private Double distanceInKm; // Dùng khi search gần đây
}
