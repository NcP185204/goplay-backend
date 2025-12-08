package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.model.CourtImage;
import com.backend.GoPlay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CourtService {

    // --- Operations CRUD ---
    CourtDetailResponse createCourt(CreateCourtRequest request, User owner);
    CourtDetailResponse updateCourt(Integer courtId, CreateCourtRequest request, User currentUser);
    void deleteCourt(Integer courtId, User currentUser);

    // --- Operations Đọc & Tìm kiếm ---
    CourtDetailResponse getCourtById(Integer courtId);
    // THAY ĐỔI KIỂU TRẢ VỀ
    Page<CourtSummaryResponse> searchCourts(CourtSearchCriteria criteria, Pageable pageable);

    // --- Operations Đánh giá ---
    ReviewResponse addReview(Integer courtId, CreateReviewRequest request, User player);
    Page<ReviewResponse> getReviews(Integer courtId, Pageable pageable);

    // --- Operations Khung giờ ---
    List<TimeSlotDto> getAvailableTimeSlots(Integer courtId, LocalDate date);
    List<TimeSlotDto> generateInitialTimeSlots(Integer courtId);

    // --- Operations Sân yêu thích ---
    void addCourtToFavorites(Integer courtId, User player);
    void removeCourtFromFavorites(Integer courtId, User player);
    List<CourtDetailResponse> getFavoriteCourts(User player);

    // --- Operations Quản lý ảnh ---
    CourtImage uploadCourtImage(Integer courtId, MultipartFile file, User manager);
    void deleteCourtImage(Integer courtId, Integer imageId, User manager);
    void setThumbnail(Integer courtId, Integer imageId, User manager);
}
