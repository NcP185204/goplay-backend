package com.backend.GoPlay.controller;

import com.backend.GoPlay.util.SportType;
import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus; // <-- CẦN THÊM
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat; // <-- CẦN THÊM

import java.time.LocalDate; // <-- CẦN THÊM
import java.util.List; // <-- CẦN THÊM


@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    // --- C. CREATE (Đã có) ---
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<CourtDetailResponse> create(@RequestBody CreateCourtRequest req, @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(courtService.createCourt(req, user), HttpStatus.CREATED);
    }

    // --- R. READ (Đã có) ---
    @GetMapping("/{id}")
    public ResponseEntity<CourtDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    // --- SEARCH (Đã có) ---
    @GetMapping("/search")
    public ResponseEntity<Page<CourtDetailResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SportType courtType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusInKm,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        CourtSearchCriteria criteria = new CourtSearchCriteria();
        criteria.setName(name); criteria.setCourtType(courtType);
        criteria.setMinPrice(minPrice); criteria.setMaxPrice(maxPrice);
        criteria.setMinRating(minRating);
        criteria.setLatitude(latitude); criteria.setLongitude(longitude);
        criteria.setRadiusInKm(radiusInKm);

        return ResponseEntity.ok(courtService.searchCourts(criteria, pageable));
    }


    // 1. UPDATE (Sửa sân)
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourtDetailResponse> updateCourt(
            @PathVariable Integer id,
            @RequestBody CreateCourtRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(courtService.updateCourt(id, request, user));
    }

    // 2. DELETE (Xóa sân)
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCourt(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        courtService.deleteCourt(id, user);
        return ResponseEntity.noContent().build();
    }

    // 3. THÊM REVIEW
    @PostMapping("/{courtId}/reviews")
    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Integer courtId,
            @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal User player
    ) {
        return new ResponseEntity<>(courtService.addReview(courtId, request, player), HttpStatus.CREATED);
    }

    // 4. LẤY DANH SÁCH REVIEW
    @GetMapping("/{courtId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable Integer courtId,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(courtService.getReviews(courtId, pageable));
    }

    // 5. LẤY KHUNG GIỜ TRỐNG
    @GetMapping("/{courtId}/available-slots")
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlots(
            @PathVariable Integer courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date // Ví dụ: ?date=2025-12-31
    ) {
        return ResponseEntity.ok(courtService.getAvailableTimeSlots(courtId, date));
    }

    // 6. TẠO KHUNG GIỜ MẪU (Cho Manager/Admin)
    @PostMapping("/{courtId}/generate-slots")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<TimeSlotDto>> generateSlots(
            @PathVariable Integer courtId
    ) {
        return new ResponseEntity<>(courtService.generateInitialTimeSlots(courtId), HttpStatus.CREATED);
    }
}