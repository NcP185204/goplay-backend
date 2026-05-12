package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.court.CreateReviewRequest;
import com.backend.GoPlay.dto.court.ReviewResponse;
import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.Court;
import com.backend.GoPlay.model.Review;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.CourtRepository;
import com.backend.GoPlay.repository.ReviewRepository;
import com.backend.GoPlay.service.ReviewService;
import com.backend.GoPlay.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourtRepository courtRepository; // Đảm bảo đã inject

    @Override
    @Transactional
    public ReviewResponse addReview(Integer courtId, CreateReviewRequest request, User player) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Sân không tồn tại"));

        if (player.getRole() != UserRole.PLAYER) {
            throw new AccessDeniedException("Chỉ người chơi (PLAYER) mới có thể đánh giá.");
        }

        Review review = Review.builder()
                .court(court)
                .player(player)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        updateCourtAverageRating(courtId);

        return mapToReviewResponse(savedReview);
    }

    @Override
    public Page<ReviewResponse> getReviews(Integer courtId, Pageable pageable) {
        if (!courtRepository.existsById(courtId)) {
            throw new ResourceNotFoundException("Sân không tồn tại");
        }
        Page<Review> reviews = reviewRepository.findByCourtId(courtId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }

    @Transactional
    public void updateCourtAverageRating(Integer courtId) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Sân không tồn tại"));
        Double avgRating = reviewRepository.calculateAverageRating(courtId); // Gọi từ reviewRepository

        if (avgRating == null) {
            court.setAverageRating(0.0);
        } else {
            court.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
        }
        courtRepository.save(court);
    }

    private ReviewResponse mapToReviewResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .playerName(r.getPlayer().getFullName())
                .playerEmail(r.getPlayer().getEmail())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
