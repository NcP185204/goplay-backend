package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.*;
import com.backend.GoPlay.repository.*;
import com.backend.GoPlay.service.CourtService;
import com.backend.GoPlay.service.specification.CourtSpecification;
import com.backend.GoPlay.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // Cần thiết cho TimeSlot
import java.time.LocalDateTime; // Cần thiết cho TimeSlot
import java.time.ZoneId; // Cần thiết cho TimeSlot
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors; // Cần thiết cho TimeSlot

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final ReviewRepository reviewRepository;
    private final CourtSpecification courtSpecification;

    private final TimeSlotRepository timeSlotRepository; // <-- ĐÃ INJECT THÊM
    // private final BookingRepository bookingRepository;


    // --- (Các hàm CRUD, Search, Review cũ giữ nguyên) ---

    // --- C. CREATE (Tạo sân) ---
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
                .imageUrls(request.getImageUrls())
                .services(request.getServices())
                .averageRating(0.0)
                .owner(owner)
                .build();
        return mapToResponse(courtRepository.save(court));
    }

    // --- R. READ (Xem chi tiết sân) ---
    @Override
    public CourtDetailResponse getCourtById(Integer id) {
        return mapToResponse(findCourtById(id));
    }

    // --- S. SEARCH (Tìm kiếm sân nâng cao) ---
    @Override
    public Page<CourtDetailResponse> searchCourts(CourtSearchCriteria criteria, Pageable pageable) {
        Page<Court> courts = courtRepository.findAll(courtSpecification.build(criteria), pageable);

        return courts.map(court -> {
            CourtDetailResponse response = mapToResponse(court);
            if (criteria.getLatitude() != null && criteria.getLongitude() != null) {
                response.setDistanceInKm(calculateDistance(
                        criteria.getLatitude(), criteria.getLongitude(),
                        court.getLatitude(), court.getLongitude()));
            }
            return response;
        });
    }

    // --- U. UPDATE (Sửa sân) ---
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
        court.setImageUrls(request.getImageUrls());
        court.setServices(request.getServices());

        return mapToResponse(courtRepository.save(court));
    }

    // --- D. DELETE (Xóa sân) ---
    @Override
    @Transactional
    public void deleteCourt(Integer courtId, User currentUser) {
        Court court = findCourtById(courtId);
        checkOwnership(court, currentUser);

        // TODO: Cần thêm logic xóa booking/review liên quan trước khi xóa sân
        courtRepository.delete(court);
    }

    // --- A. ADD REVIEW (Thêm đánh giá) ---
    @Override
    @Transactional
    public ReviewResponse addReview(Integer courtId, CreateReviewRequest request, User player) {
        Court court = findCourtById(courtId);

        if (player.getRole() != UserRole.PLAYER) {
            throw new AccessDeniedException("Chỉ người chơi (PLAYER) mới có thể đánh giá.");
        }

        // TODO: Thêm logic kiểm tra xem người chơi đã từng đặt sân này chưa

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

    // --- G. GET REVIEWS (Lấy danh sách đánh giá) ---
    @Override
    public Page<ReviewResponse> getReviews(Integer courtId, Pageable pageable) {
        if (!courtRepository.existsById(courtId)) {
            throw new ResourceNotFoundException("Sân không tồn tại");
        }
        Page<Review> reviews = reviewRepository.findByCourtId(courtId, pageable);
        return reviews.map(this::mapToReviewResponse);
    }


    // ==========================================================
    // --- BỔ SUNG: TRIỂN KHAI TIMESLOT BỊ THIẾU ---
    // ==========================================================

    // --- T1. GET AVAILABLE TIMESLOTS (Lấy khung giờ trống) ---
    @Override
    public List<TimeSlotDto> getAvailableTimeSlots(Integer courtId, LocalDate date) {
        Court court = findCourtById(courtId); // Kiểm tra sân có tồn tại không

        // Xác định mốc thời gian (00:00:00 và 23:59:59 của ngày đó)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // Gọi Repository để tìm
        List<TimeSlot> slots = timeSlotRepository.findAvailableSlotsByCourtAndDate(
                courtId, startOfDay, endOfDay
        );

        // Chuyển đổi List<TimeSlot> (model) thành List<TimeSlotDto> (dto)
        return slots.stream()
                .map(this::mapToTimeSlotDto)
                .collect(Collectors.toList());
    }

    // --- T2. GENERATE TIMESLOTS (Tạo khung giờ ban đầu cho Manager) ---
    @Override
    @Transactional
    public List<TimeSlotDto> generateInitialTimeSlots(Integer courtId) {
        Court court = findCourtById(courtId); // Kiểm tra sân có tồn tại không
        List<TimeSlot> newSlots = new java.util.ArrayList<>();

        // Logic: Tạo khung giờ 2 tiếng, từ 8:00 đến 22:00, cho 7 ngày tới.
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
                        .isAvailable(true) // Mặc định là trống
                        .build();
                newSlots.add(slot);
            }
        }

        // Lưu hàng loạt vào CSDL
        timeSlotRepository.saveAll(newSlots);

        // Trả về DTO
        return newSlots.stream()
                .map(this::mapToTimeSlotDto)
                .collect(Collectors.toList());
    }

    // ==========================================================
    // --- HÀM HELPER VÀ LOGIC NỘI BỘ (Giữ nguyên) ---
    // ==========================================================

    // Hàm tìm Court (hoặc ném 404)
    private Court findCourtById(Integer courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Sân không tồn tại"));
    }

    // Hàm kiểm tra quyền sở hữu
    private void checkOwnership(Court court, User currentUser) {
        boolean isOwner = Objects.equals(court.getOwner().getId(), currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này.");
        }
    }

    // Hàm cập nhật Rating trung bình
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

    // Hàm Map từ Entity sang DTO (cho Detail/Search)
    private CourtDetailResponse mapToResponse(Court c) {
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
                .imageUrls(c.getImageUrls())
                .services(c.getServices())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .build();
    }

    // Hàm Map từ Review Entity sang ReviewResponse DTO
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

    // Hàm Map từ TimeSlot Entity sang TimeSlotResponse DTO
    private TimeSlotDto mapToTimeSlotDto(TimeSlot slot) {
        // Chuyển LocalDateTime sang Timestamp (Long) để trả về cho Android
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


    // Công thức Haversine tính khoảng cách (km)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính trái đất (Kilometers)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return Math.round(R * c * 10.0) / 10.0;
    }
}