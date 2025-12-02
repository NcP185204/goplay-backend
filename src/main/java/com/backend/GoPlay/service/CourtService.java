package com.backend.GoPlay.service;


import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate; // Cần thiết
import java.util.List; // Cần thiết

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

    /**
     * Lấy danh sách khung giờ trống cho 1 sân trong 1 ngày cụ thể.
     */
    List<TimeSlotDto> getAvailableTimeSlots(Integer courtId, LocalDate date);

    /**
     * [Mở rộng] Tạo hàng loạt khung giờ mặc định (cho Manager).
     */
    List<TimeSlotDto> generateInitialTimeSlots(Integer courtId);
}