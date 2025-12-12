package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.model.CourtImage;
import com.backend.GoPlay.model.PricingRule;
import com.backend.GoPlay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CourtService {

    // --- Court CRUD ---
    CourtDetailResponse createCourt(CreateCourtRequest request, User owner);
    CourtDetailResponse updateCourt(Integer courtId, CreateCourtRequest request, User currentUser);
    void deleteCourt(Integer courtId, User currentUser);

    // --- Court Read & Search ---
    CourtDetailResponse getCourtById(Integer courtId);
    Page<CourtSummaryResponse> searchCourts(CourtSearchCriteria criteria, Pageable pageable);

    // --- Review ---
    ReviewResponse addReview(Integer courtId, CreateReviewRequest request, User player);
    Page<ReviewResponse> getReviews(Integer courtId, Pageable pageable);

    // --- TimeSlot ---
    List<TimeSlotDto> getAvailableTimeSlots(Integer courtId, LocalDate date);
    List<TimeSlotDto> generateInitialTimeSlots(Integer courtId, GenerateTimeSlotRequest request);

    // --- Favorite ---
    void addCourtToFavorites(Integer courtId, User player);
    void removeCourtFromFavorites(Integer courtId, User player);
    List<CourtDetailResponse> getFavoriteCourts(User player);

    // --- Image Management ---
    CourtImage uploadCourtImage(Integer courtId, MultipartFile file, User manager);
    void deleteCourtImage(Integer courtId, Integer imageId, User manager);
    void setThumbnail(Integer courtId, Integer imageId, User manager);

    // --- Pricing Rule Management ---
    PricingRule setPricingRule(Integer courtId, PricingRuleDto dto, User manager);
    List<PricingRule> getPricingRules(Integer courtId);
    void deletePricingRule(Integer courtId, Integer ruleId, User manager);
}
