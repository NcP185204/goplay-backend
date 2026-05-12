package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.court.*;
import com.backend.GoPlay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourtService {

    // --- Operations CRUD ---
    CourtDetailResponse createCourt(CreateCourtRequest request, User owner);
    CourtDetailResponse updateCourt(Integer courtId, CreateCourtRequest request, User currentUser);
    void deleteCourt(Integer courtId, User currentUser);

    // --- Operations Đọc & Tìm kiếm ---
    CourtDetailResponse getCourtById(Integer courtId);
    Page<CourtSummaryResponse> searchCourts(CourtSearchCriteria criteria, Pageable pageable);
}
