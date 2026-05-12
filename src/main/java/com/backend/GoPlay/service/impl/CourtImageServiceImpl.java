package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.Court;
import com.backend.GoPlay.model.CourtImage;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.CourtImageRepository;
import com.backend.GoPlay.repository.CourtRepository;
import com.backend.GoPlay.service.CourtImageService;
import com.backend.GoPlay.service.StorageService;
import com.backend.GoPlay.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CourtImageServiceImpl implements CourtImageService {

    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final StorageService storageService; // Inject Interface, không phải Class cụ thể

    @Override
    @Transactional
    public CourtImage uploadCourtImage(Integer courtId, MultipartFile file, User manager) {
        Court court = findCourtById(courtId);
        checkOwnership(court, manager);

        String imageUrl = storageService.store(file, "courts");

        CourtImage newImage = CourtImage.builder()
                .imageUrl(imageUrl)
                .court(court)
                .build();

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

        CourtImage image = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh"));

        if (!image.getCourt().getId().equals(courtId)) {
            throw new AccessDeniedException("Ảnh không thuộc về sân này");
        }

        storageService.delete(image.getImageUrl());
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

        CourtImage image = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh"));

        if (!image.getCourt().getId().equals(courtId)) {
            throw new AccessDeniedException("Ảnh không thuộc về sân này");
        }

        court.setThumbnailUrl(image.getImageUrl());
        courtRepository.save(court);
    }

    // Helper methods (DRY: Có thể tách ra class Utility riêng nếu muốn dùng chung)
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
}
