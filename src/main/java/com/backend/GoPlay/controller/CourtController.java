package com.backend.GoPlay.controller;

import com.backend.GoPlay.model.CourtImage;
import com.backend.GoPlay.util.SportType;
import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    // ... (các API khác giữ nguyên)

    // --- API SEARCH ĐÃ ĐƯỢC TỐI ƯU HÓA ---
    @GetMapping("/search")
    public ResponseEntity<Page<CourtSummaryResponse>> search( // THAY ĐỔI KIỂU TRẢ VỀ
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

    // ... (các API khác giữ nguyên)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<CourtDetailResponse> create(@RequestBody CreateCourtRequest req, @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(courtService.createCourt(req, user), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourtDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(courtService.getCourtById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourtDetailResponse> updateCourt(
            @PathVariable Integer id,
            @RequestBody CreateCourtRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(courtService.updateCourt(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCourt(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {
        courtService.deleteCourt(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courtId}/reviews")
    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Integer courtId,
            @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal User player
    ) {
        return new ResponseEntity<>(courtService.addReview(courtId, request, player), HttpStatus.CREATED);
    }

    @GetMapping("/{courtId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable Integer courtId,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(courtService.getReviews(courtId, pageable));
    }

    @GetMapping("/{courtId}/available-slots")
    public ResponseEntity<List<TimeSlotDto>> getAvailableSlots(
            @PathVariable Integer courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(courtService.getAvailableTimeSlots(courtId, date));
    }

    @PostMapping("/{courtId}/generate-slots")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<TimeSlotDto>> generateSlots(
            @PathVariable Integer courtId
    ) {
        return new ResponseEntity<>(courtService.generateInitialTimeSlots(courtId), HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<CourtImage> uploadImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User manager
    ) {
        CourtImage newImage = courtService.uploadCourtImage(id, file, manager);
        return new ResponseEntity<>(newImage, HttpStatus.CREATED);
    }

    @DeleteMapping("/{courtId}/images/{imageId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Integer courtId,
            @PathVariable Integer imageId,
            @AuthenticationPrincipal User manager
    ) {
        courtService.deleteCourtImage(courtId, imageId, manager);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{courtId}/thumbnail/{imageId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> setThumbnail(
            @PathVariable Integer courtId,
            @PathVariable Integer imageId,
            @AuthenticationPrincipal User manager
    ) {
        courtService.setThumbnail(courtId, imageId, manager);
        return ResponseEntity.ok().build();
    }
}
