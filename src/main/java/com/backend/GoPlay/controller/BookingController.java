package com.backend.GoPlay.controller;

import com.backend.GoPlay.dto.booking.BookingRequest;
import com.backend.GoPlay.dto.booking.BookingResponse;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // API Đặt sân
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(bookingService.createBooking(request, user));
    }

    // API Xem lịch sử đặt sân của tôi
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(bookingService.getUserBookings(user));
    }

    // --- API MỚI: HỦY ĐẶT SÂN ---
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Integer bookingId,
            @AuthenticationPrincipal User user
    ) {
        bookingService.cancelBooking(bookingId, user);
        return ResponseEntity.ok().build();
    }

    // --- API MỚI: LẤY TRẬN ĐẤU SẮP TỚI ---
    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> getUpcomingBooking(
            @AuthenticationPrincipal User user
    ) {
        Optional<BookingResponse> upcomingBooking = bookingService.getUpcomingBooking(user);

        // Nếu có trận đấu, trả về 200 OK với dữ liệu. Nếu không, trả về 204 No Content
        return upcomingBooking
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
