package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.court.CreateReviewRequest;
import com.backend.GoPlay.dto.court.ReviewResponse;
import com.backend.GoPlay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    /**
     * Thêm một đánh giá mới cho một sân.
     * @param courtId ID của sân được đánh giá.
     * @param request DTO chứa thông tin đánh giá.
     * @param player Người dùng đang thực hiện đánh giá.
     * @return DTO của đánh giá vừa được tạo.
     */
    ReviewResponse addReview(Integer courtId, CreateReviewRequest request, User player);

    /**
     * Lấy danh sách các đánh giá của một sân (có phân trang).
     * @param courtId ID của sân.
     * @param pageable Thông tin phân trang.
     * @return Một trang các đánh giá.
     */
    Page<ReviewResponse> getReviews(Integer courtId, Pageable pageable);
}
