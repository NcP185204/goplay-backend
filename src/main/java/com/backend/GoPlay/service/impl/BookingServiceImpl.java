package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.booking.BookingRequest;
import com.backend.GoPlay.dto.booking.BookingResponse;
import com.backend.GoPlay.event.BookingConfirmedEvent;
import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.Booking;
import com.backend.GoPlay.model.Payment;
import com.backend.GoPlay.model.TimeSlot;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.BookingRepository;
import com.backend.GoPlay.repository.TimeSlotRepository;
import com.backend.GoPlay.service.BookingService;
import com.backend.GoPlay.util.BookingStatus;
import com.backend.GoPlay.util.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ApplicationEventPublisher eventPublisher; // Thêm công cụ phát sự kiện

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, User user) {
        
        if (request.getTimeSlotIds() == null || request.getTimeSlotIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một khung giờ để đặt sân.");
        }

        List<TimeSlot> slotsToBook = new ArrayList<>();
        double totalPrice = 0.0;
        Integer firstCourtId = null;

        for (Integer slotId : request.getTimeSlotIds()) {
            TimeSlot slot = timeSlotRepository.findById(slotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Khung giờ không tồn tại: " + slotId));

            if (firstCourtId == null) {
                firstCourtId = slot.getCourt().getId();
            } else if (!Objects.equals(firstCourtId, slot.getCourt().getId())) {
                throw new IllegalArgumentException("Không thể đặt các khung giờ thuộc về nhiều sân khác nhau trong cùng một đơn hàng.");
            }

            if (!slot.isAvailable()) {
                throw new IllegalStateException("Khung giờ " + slot.getStartTime() + " đã bị người khác đặt!");
            }

            slot.setAvailable(false);
            timeSlotRepository.save(slot);

            Double price = slot.getPrice() != null ? slot.getPrice() : 0.0;
            totalPrice += price;
            
            slotsToBook.add(slot);
        }

        // Kiểm tra và chuẩn hóa tên phương thức thanh toán
        String paymentMethod = (request.getPaymentMethod() != null) ? request.getPaymentMethod().toUpperCase() : "MOMO";
        
        // Nếu là CASH, đơn hàng sẽ CONFIRMED ngay lập tức (không cần chờ thanh toán online)
        BookingStatus initialBookingStatus = paymentMethod.equals("CASH") ? BookingStatus.CONFIRMED : BookingStatus.PENDING;
        PaymentStatus initialPaymentStatus = paymentMethod.equals("CASH") ? PaymentStatus.PENDING : PaymentStatus.PENDING;

        Booking booking = Booking.builder()
                .user(user)
                .timeSlots(slotsToBook)
                .totalPrice(totalPrice)
                .status(initialBookingStatus) // Trạng thái tùy theo hình thức
                .createdAt(LocalDateTime.now())
                .note(request.getNote())
                .build();

        Payment payment = Payment.builder()
                .booking(booking)
                .user(user)
                .amount(totalPrice)
                .status(initialPaymentStatus)
                .paymentMethod(paymentMethod) // Lấy từ request
                .createdAt(LocalDateTime.now())
                .build();

        booking.setPayment(payment);

        Booking savedBooking = bookingRepository.save(booking);

        // Nếu đơn hàng được xác nhận ngay lập tức (CASH), hãy phát sự kiện
        if (savedBooking.getStatus() == BookingStatus.CONFIRMED) {
            eventPublisher.publishEvent(new BookingConfirmedEvent(this, savedBooking));
        }

        return mapToResponse(savedBooking);
    }

    @Override
    public List<BookingResponse> getUserBookings(User user) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return bookings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelBooking(Integer bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân với ID: " + bookingId));

        if (!Objects.equals(booking.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền hủy đơn đặt sân này.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Đơn này đã được hủy trước đó.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        for (TimeSlot slot : booking.getTimeSlots()) {
            slot.setAvailable(true);
            timeSlotRepository.save(slot);
        }
        
        Payment payment = booking.getPayment();
        if(payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.FAILED); 
        }
    }

    @Override
    public Optional<BookingResponse> getUpcomingBooking(User user) {
        // Lấy thời gian hiện tại
        LocalDateTime currentTime = LocalDateTime.now();

        // Gọi Repository để lấy trận đấu sắp tới (đã được CONFIRMED và thời gian lớn hơn hiện tại)
        Optional<Booking> upcomingBooking = bookingRepository.findUpcomingBooking(
                user.getId(), 
                BookingStatus.CONFIRMED, 
                currentTime
        );

        // Nếu tìm thấy, chuyển đổi sang BookingResponse và trả về. Nếu không, trả về Optional.empty()
        return upcomingBooking.map(this::mapToResponse);
    }


    private BookingResponse mapToResponse(Booking booking) {
        String courtName = "";
        String courtAddress = "";
        if (!booking.getTimeSlots().isEmpty()) {
            courtName = booking.getTimeSlots().get(0).getCourt().getName();
            courtAddress = booking.getTimeSlots().get(0).getCourt().getAddress();
        }

        List<String> slotDetails = booking.getTimeSlots().stream()
                .map(slot -> slot.getStartTime().toLocalTime() + " - " + slot.getEndTime().toLocalTime())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .courtName(courtName)
                .courtAddress(courtAddress)
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .timeSlotDetails(slotDetails)
                .build();
    }
}
