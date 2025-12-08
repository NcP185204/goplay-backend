package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.*;
import com.backend.GoPlay.repository.*;
import com.backend.GoPlay.service.CourtService;
import com.backend.GoPlay.service.FileStorageService;
import com.backend.GoPlay.service.specification.CourtSpecification;
import com.backend.GoPlay.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final ReviewRepository reviewRepository;
    private final CourtSpecification courtSpecification;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CourtImageRepository courtImageRepository;

    // --- (Các hàm CRUD, Review, Image... giữ nguyên) ---
    // ...

    // ==========================================================
    // --- TỐI ƯU HÓA: SỬA LẠI HÀM SEARCH ---
    // ==========================================================
    @Override
    public Page<CourtSummaryResponse> searchCourts(CourtSearchCriteria criteria, Pageable pageable) {
        Page<Court> courts = courtRepository.findAll(courtSpecification.build(criteria), pageable);

        // Dùng hàm map của Page để chuyển đổi từ Page<Court> sang Page<CourtSummaryResponse>
        return courts.map(court -> {
            CourtSummaryResponse summary = mapToSummaryResponse(court);
            // Tính toán khoảng cách nếu có thông tin vị trí
            if (criteria.getLatitude() != null && criteria.getLongitude() != null) {
                summary.setDistanceInKm(calculateDistance(
                        criteria.getLatitude(), criteria.getLongitude(),
                        court.getLatitude(), court.getLongitude()));
            }
            return summary;
        });
    }

    // --- (Các hàm khác giữ nguyên) ---
    // ...

    // ==========================================================
    // --- HÀM HELPER VÀ LOGIC NỘI BỘ ---
    // ==========================================================

    private Court findCourtById(Integer courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Sân không tồn tại"));
    }

    private void checkOwnership(Court court, User currentUser) {
        boolean isOwner = Objects.equals(court.getOwner().getId(), currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này.");
        }
    }

    // --- HÀM MAPPER MỚI CHO SUMMARY ---
    private CourtSummaryResponse mapToSummaryResponse(Court c) {
        return CourtSummaryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .address(c.getAddress())
                .courtType(c.getCourtType())
                .pricePerHour(c.getPricePerHour())
                .averageRating(c.getAverageRating())
                .thumbnailUrl(c.getThumbnailUrl()) // Chỉ lấy thumbnail
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .build();
    }

    // --- HÀM MAPPER CŨ CHO DETAIL (Vẫn giữ lại để dùng cho getCourtById) ---
    private CourtDetailResponse mapToResponse(Court c) {
        // Dòng này sẽ kích hoạt Lazy Loading, chỉ nên dùng trong hàm getCourtById
        List<String> imageUrls = c.getImages().stream()
                                  .map(CourtImage::getImageUrl)
                                  .collect(Collectors.toList());

        return CourtDetailResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .address(c.getAddress())
                .description(c.getDescription())
                .courtType(c.getCourtType())
                .pricePerHour(c.getPricePerHour())
                .averageRating(c.getAverageRating())
                .ownerName(c.getOwner().getFullName())
                .ownerEmail(c.getOwner().getEmail())
                .thumbnailUrl(c.getThumbnailUrl())
                .imageUrls(imageUrls) // Lấy toàn bộ album
                .services(c.getServices())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .build();
    }
    
    // ... (các hàm còn lại giữ nguyên)
    @Override
    @Transactional
    public CourtDetailResponse createCourt(CreateCourtRequest request, User owner) {
        Court court = Court.builder()
                .name(request.getName())
                .address(request.getAddress())
                .description(request.getDescription())
                .courtType(request.getCourtType())
                .pricePerHour(request.getPricePerHour())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .averageRating(0.0)
                .owner(owner)
                .services(request.getServices())
                .build();
        return mapToResponse(courtRepository.save(court));
    }

    @Override
    @Transactional
    public CourtDetailResponse updateCourt(Integer courtId, CreateCourtRequest request, User currentUser) {
        Court court = findCourtById(courtId);
        checkOwnership(court, currentUser);

        court.setName(request.getName());
        court.setAddress(request.getAddress());
        court.setDescription(request.getDescription());
        court.setCourtType(request.getCourtType());
        court.setPricePerHour(request.getPricePerHour());
        court.setLatitude(request.getLatitude());
        court.setLongitude(request.getLongitude());
        court.setServices(request.getServices());

        return mapToResponse(courtRepository.save(court));
    }

    @Override
    @Transactional
    public void deleteCourt(Integer courtId, User currentUser) {
        Court court = findCourtById(courtId);
        checkOwnership(court, currentUser);
        courtRepository.delete(court);
    }

    @Override
    public CourtDetailResponse getCourtById(Integer courtId) {
        return mapToResponse(findCourtById(courtId));
    }

    @Override
    @Transactional
    public ReviewResponse addReview(Integer courtId, CreateReviewRequest request, User player) {
        Court court = findCourtById(courtId);
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

    @Override
    public List<TimeSlotDto> getAvailableTimeSlots(Integer courtId, LocalDate date) {
        findCourtById(courtId);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<TimeSlot> slots = timeSlotRepository.findAvailableSlotsByCourtAndDate(courtId, startOfDay, endOfDay);
        return slots.stream().map(this::mapToTimeSlotDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TimeSlotDto> generateInitialTimeSlots(Integer courtId) {
        Court court = findCourtById(courtId);
        List<TimeSlot> newSlots = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            for (int hour = 8; hour <= 20; hour += 2) {
                LocalDateTime startTime = date.atTime(hour, 0);
                LocalDateTime endTime = date.atTime(hour + 2, 0);
                TimeSlot slot = TimeSlot.builder()
                        .court(court)
                        .startTime(startTime)
                        .endTime(endTime)
                        .isAvailable(true)
                        .build();
                newSlots.add(slot);
            }
        }
        timeSlotRepository.saveAll(newSlots);
        return newSlots.stream().map(this::mapToTimeSlotDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addCourtToFavorites(Integer courtId, User player) {
        User managedPlayer = userRepository.findById(player.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Court court = findCourtById(courtId);
        managedPlayer.getFavoriteCourts().add(court);
    }

    @Override
    @Transactional
    public void removeCourtFromFavorites(Integer courtId, User player) {
        User managedPlayer = userRepository.findById(player.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Court court = findCourtById(courtId);
        managedPlayer.getFavoriteCourts().remove(court);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtDetailResponse> getFavoriteCourts(User player) {
        User userWithFavorites = userRepository.findById(player.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userWithFavorites.getFavoriteCourts().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourtImage uploadCourtImage(Integer courtId, MultipartFile file, User manager) {
        Court court = findCourtById(courtId);
        checkOwnership(court, manager);
        String imageUrl = fileStorageService.store(file, "courts");
        CourtImage newImage = CourtImage.builder().imageUrl(imageUrl).court(court).build();
        if (court.getThumbnailUrl() == null || court.getThumbnailUrl().isEmpty()) {
            court.setThumbnailUrl(imageUrl);
            courtRepository.save(court);
        }
        return courtImageRepository.save(newImage);
    }

    @Override
    @Transactional
    public void deleteCourtImage(Integer courtId, Integer imageId, User manager) {
        Court court = findCourtById(courtId);
        checkOwnership(court, manager);
        CourtImage image = courtImageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh"));
        if (!image.getCourt().getId().equals(courtId)) {
            throw new AccessDeniedException("Ảnh không thuộc về sân này");
        }
        fileStorageService.delete(image.getImageUrl());
        courtImageRepository.delete(image);
        if (image.getImageUrl().equals(court.getThumbnailUrl())) {
            court.setThumbnailUrl(null);
            courtRepository.save(court);
        }
    }

    @Override
    @Transactional
    public void setThumbnail(Integer courtId, Integer imageId, User manager) {
        Court court = findCourtById(courtId);
        checkOwnership(court, manager);
        CourtImage image = courtImageRepository.findById(imageId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh"));
        if (!image.getCourt().getId().equals(courtId)) {
            throw new AccessDeniedException("Ảnh không thuộc về sân này");
        }
        court.setThumbnailUrl(image.getImageUrl());
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

    private TimeSlotDto mapToTimeSlotDto(TimeSlot slot) {
        long startTimeMillis = slot.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimeMillis = slot.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return TimeSlotDto.builder()
                .id(slot.getId().toString())
                .courtId(slot.getCourt().getId().toString())
                .startTime(startTimeMillis)
                .endTime(endTimeMillis)
                .isAvailable(slot.isAvailable())
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return Math.round(R * c * 10.0) / 10.0;
    }
    
    @Transactional
    public void updateCourtAverageRating(Integer courtId) {
        Double avgRating = reviewRepository.calculateAverageRating(courtId);
        Court court = findCourtById(courtId);
        if (avgRating == null) {
            court.setAverageRating(0.0);
        } else {
            court.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
        }
        courtRepository.save(court);
    }
}
