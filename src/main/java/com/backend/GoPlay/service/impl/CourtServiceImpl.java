package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.Court;
import com.backend.GoPlay.model.CourtImage;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.CourtRepository;
import com.backend.GoPlay.service.CourtService;
import com.backend.GoPlay.service.specification.CourtSpecification;
import com.backend.GoPlay.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final CourtSpecification courtSpecification;

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
    public Page<CourtSummaryResponse> searchCourts(CourtSearchCriteria criteria, Pageable pageable) {
        Page<Court> courts = courtRepository.findAll(courtSpecification.build(criteria), pageable);
        return courts.map(this::mapToSummaryResponse);
    }

    private Court findCourtById(Integer courtId) {
        return courtRepository.findById(courtId).orElseThrow(() -> new ResourceNotFoundException("Sân không tồn tại"));
    }

    private void checkOwnership(Court court, User currentUser) {
        boolean isOwner = Objects.equals(court.getOwner().getId(), currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này.");
        }
    }

    private CourtDetailResponse mapToResponse(Court c) {
        List<String> imageUrls = c.getImages().stream().map(CourtImage::getImageUrl).collect(Collectors.toList());
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
                .imageUrls(imageUrls)
                .services(c.getServices())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .build();
    }
    
    private CourtSummaryResponse mapToSummaryResponse(Court c) {
        return CourtSummaryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .address(c.getAddress())
                .courtType(c.getCourtType())
                .pricePerHour(c.getPricePerHour())
                .averageRating(c.getAverageRating())
                .thumbnailUrl(c.getThumbnailUrl())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .build();
    }
}
