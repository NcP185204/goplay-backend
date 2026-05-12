package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.booking.BookingRequest;
import com.backend.GoPlay.dto.booking.BookingResponse;
import com.backend.GoPlay.model.User;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    // Hàm tạo đơn đặt sân
    BookingResponse createBooking(BookingRequest request, User user);
    
    // Hàm xem lịch sử đặt sân
    List<BookingResponse> getUserBookings(User user);

    // Hàm hủy đặt sân
    void cancelBooking(Integer bookingId, User user);

    // Hàm lấy trận đấu sắp tới
    Optional<BookingResponse> getUpcomingBooking(User user);


}
