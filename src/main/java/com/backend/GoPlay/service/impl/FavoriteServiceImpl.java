package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.court.CourtDetailResponse;
import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.Court;
import com.backend.GoPlay.model.CourtImage;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.CourtRepository;
import com.backend.GoPlay.repository.UserRepository;
import com.backend.GoPlay.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserRepository userRepository;
    private final CourtRepository courtRepository;

    @Override
    @Transactional
    public void addCourtToFavorites(Integer courtId, User player) {
        User managedPlayer = findUserById(player.getId());
        Court court = findCourtById(courtId);
        managedPlayer.getFavoriteCourts().add(court);
    }

    @Override
    @Transactional
    public void removeCourtFromFavorites(Integer courtId, User player) {
        User managedPlayer = findUserById(player.getId());
        Court court = findCourtById(courtId);
        managedPlayer.getFavoriteCourts().remove(court);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtDetailResponse> getFavoriteCourts(User player) {
        User userWithFavorites = findUserById(player.getId());
        return userWithFavorites.getFavoriteCourts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Court findCourtById(Integer courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Sân không tồn tại"));
    }

    // Hàm map này có thể được tách ra một class Mapper chung để tuân thủ DRY tốt hơn
    private CourtDetailResponse mapToResponse(Court c) {
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
                .imageUrls(imageUrls)
                .services(c.getServices())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .build();
    }
}
