package com.backend.GoPlay.model;

import com.backend.GoPlay.util.FacilityService;
import com.backend.GoPlay.util.SportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courts", indexes = {
        @Index(name = "idx_court_name", columnList = "name"),
        @Index(name = "idx_court_type", columnList = "courtType"),
        @Index(name = "idx_price", columnList = "pricePerHour")
})
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String address;
    @Column(columnDefinition = "TEXT") private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private SportType courtType;

    private Double pricePerHour;
    private Double latitude;
    private Double longitude;
    private Double averageRating = 0.0;

    // --- CỘT MỚI CHO ẢNH ĐẠI DIỆN ---
    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

    // --- MỐI QUAN HỆ 1-N VỚI BẢNG COURT_IMAGES ---
    // Thay thế cho @ElementCollection cũ
    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourtImage> images = new ArrayList<>();

    // --- TIỆN ÍCH (Giữ nguyên) ---
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "court_services", joinColumns = @JoinColumn(name = "court_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service")
    private Set<FacilityService> services;
}
