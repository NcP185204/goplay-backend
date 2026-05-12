package com.backend.GoPlay.controller;

import com.backend.GoPlay.dto.court.CourtDetailResponse;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.service.FavoriteService;
import com.backend.GoPlay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    // --- Favorite Courts ---
    @PostMapping("/me/favorite-courts/{courtId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> addFavorite(@PathVariable Integer courtId, @AuthenticationPrincipal User user) {
        favoriteService.addCourtToFavorites(courtId, user);
        return ResponseEntity.ok("Đã thêm vào danh sách yêu thích");
    }

    @DeleteMapping("/me/favorite-courts/{courtId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> removeFavorite(@PathVariable Integer courtId, @AuthenticationPrincipal User user) {
        favoriteService.removeCourtFromFavorites(courtId, user);
        return ResponseEntity.ok("Đã xóa khỏi danh sách yêu thích");
    }

    @GetMapping("/me/favorite-courts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourtDetailResponse>> getFavorites(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(favoriteService.getFavoriteCourts(user));
    }

    // --- FCM Token ---
    @PutMapping("/me/fcm-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateFcmToken(
            @AuthenticationPrincipal User user,
            @RequestParam String token) {
        userService.updateFcmToken(user, token);
        return ResponseEntity.ok().build();
    }
}
