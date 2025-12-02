package com.backend.GoPlay.dto.court;


import lombok.Data;

@Data
public class CreateReviewRequest {

    // Điểm đánh giá (thường là 1 đến 5)
    private int rating;

    // Nội dung bình luận
    private String comment;
}
