package com.backend.GoPlay.dto.court;



import com.backend.GoPlay.util.FacilityService;
import com.backend.GoPlay.util.SportType;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class CreateCourtRequest {
    private String name;
    private String address;
    private String description;
    private SportType courtType; // ENUM
    private Double pricePerHour;
    private Double latitude;
    private Double longitude;
    private List<String> imageUrls;
    private Set<FacilityService> services;
}