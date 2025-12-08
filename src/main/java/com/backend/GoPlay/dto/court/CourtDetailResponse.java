package com.backend.GoPlay.dto.court;

import com.backend.GoPlay.util.FacilityService;
import com.backend.GoPlay.util.SportType;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data @Builder
public class CourtDetailResponse {
    private Integer id;
    private String name;
    private String address;
    private String description;
    private SportType courtType;
    private Double pricePerHour;
    private Double averageRating;
    private String ownerName;
    private String ownerEmail;
    private String thumbnailUrl; // THÊM TRƯỜNG NÀY
    private List<String> imageUrls;
    private Set<FacilityService> services;
    private Double latitude;
    private Double longitude;
    private Double distanceInKm; // Dùng khi search gần đây
}
