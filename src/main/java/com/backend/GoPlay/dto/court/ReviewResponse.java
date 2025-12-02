package com.backend.GoPlay.dto.court;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {

    private Integer id;

    // Thông tin người đánh giá (tên, email)
    private String playerName;
    private String playerEmail;

    // Nội dung đánh giá
    private int rating;
    private String comment;

    // Thời điểm đánh giá
    private LocalDateTime createdAt;
}
