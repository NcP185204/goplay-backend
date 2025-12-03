package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CourtService {

    // --- 1. Operations CRUD (C, U, D) ---
    CourtDetailResponse createCourt(CreateCourtRequest request, User owner);
    CourtDetailResponse updateCourt(Integer courtId, CreateCourtRequest request, User currentUser);
    void deleteCourt(Integer courtId, User currentUser);

    // --- 2. Operations Đọc (R) & Tìm kiếm ---
    CourtDetailResponse getCourtById(Integer courtId);
    Page<CourtDetailResponse> searchCourts(CourtSearchCriteria criteria, Pageable pageable);

    // --- 3. Operations Đánh giá (Review) ---
    ReviewResponse addReview(Integer courtId, CreateReviewRequest request, User player);
    Page<ReviewResponse> getReviews(Integer courtId, Pageable pageable);

    // --- 4. Operations Quản lý Khung giờ (TimeSlot) ---
    List<TimeSlotDto> getAvailableTimeSlots(Integer courtId, LocalDate date);
    List<TimeSlotDto> generateInitialTimeSlots(Integer courtId);

    // --- 5. Operations Sân yêu thích (Favorite) ---
    void addCourtToFavorites(Integer courtId, User player);
    void removeCourtFromFavorites(Integer courtId, User player);
    List<CourtDetailResponse> getFavoriteCourts(User player);
}
