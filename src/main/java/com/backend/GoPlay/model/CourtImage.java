package com.backend.GoPlay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "court_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Lưu đường dẫn (URL) đến ảnh, ví dụ: "/uploads/courts/abc-123.jpg"
    @Column(nullable = false, length = 512)
    private String imageUrl;

    // Quan hệ nhiều-một: Nhiều ảnh thuộc về một sân
    // Dùng JsonIgnore để tránh vòng lặp vô hạn khi serialize
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    @JsonIgnore
    private Court court;
}
