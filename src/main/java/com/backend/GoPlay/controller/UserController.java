package com.backend.GoPlay.controller;

import com.backend.GoPlay.dto.court.CourtDetailResponse;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me") // Tất cả API liên quan đến "tôi" (người dùng đang đăng nhập)
@RequiredArgsConstructor
public class UserController {

    private final CourtService courtService;

    // --- API SÂN YÊU THÍCH ---

    /**
     * Lấy danh sách các sân yêu thích của người dùng.
     */
    @GetMapping("/favorite-courts")
    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    public ResponseEntity<List<CourtDetailResponse>> getMyFavoriteCourts(
            @AuthenticationPrincipal User player
    ) {
        return ResponseEntity.ok(courtService.getFavoriteCourts(player));
    }

    /**
     * Thêm một sân vào danh sách yêu thích.
     */
    @PostMapping("/favorite-courts/{courtId}")
    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    public ResponseEntity<Void> addCourtToFavorites(
            @PathVariable Integer courtId,
            @AuthenticationPrincipal User player
    ) {
        courtService.addCourtToFavorites(courtId, player);
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa một sân khỏi danh sách yêu thích.
     */
    @DeleteMapping("/favorite-courts/{courtId}")
    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    public ResponseEntity<Void> removeCourtFromFavorites(
            @PathVariable Integer courtId,
            @AuthenticationPrincipal User player
    ) {
        courtService.removeCourtFromFavorites(courtId, player);
        return ResponseEntity.noContent().build();
    }
}
