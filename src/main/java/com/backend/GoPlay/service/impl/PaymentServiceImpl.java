package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.payment.PaymentResponse;
import com.backend.GoPlay.event.BookingConfirmedEvent;
import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.Booking;
import com.backend.GoPlay.model.Payment;
import com.backend.GoPlay.model.TimeSlot;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.BookingRepository;
import com.backend.GoPlay.repository.PaymentRepository;
import com.backend.GoPlay.repository.TimeSlotRepository;
import com.backend.GoPlay.service.PaymentService;
import com.backend.GoPlay.service.strategy.PaymentStrategy;
import com.backend.GoPlay.service.strategy.PaymentStrategyFactory;
import com.backend.GoPlay.util.BookingStatus;
import com.backend.GoPlay.util.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TimeSlotRepository timeSlotRepository; // Thêm repo này để cập nhật trạng thái sân
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse createPayment(Integer bookingId, User user) {
        Booking booking = getBookingAndCheckOwnership(bookingId, user);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ thanh toán.");
        }

        Payment payment = booking.getPayment();
        if (payment == null) {
            throw new IllegalStateException("Không tìm thấy thông tin thanh toán cho đơn hàng này.");
        }

        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(payment.getPaymentMethod());

        String transactionId = "MOMO_" + UUID.randomUUID().toString();

        PaymentResponse paymentResponse = strategy.createPaymentRequest(transactionId, payment.getAmount());

        payment.setTransactionId(transactionId);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return paymentResponse;
    }

    @Override
    @Transactional
    public void handleSuccessfulPayment(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch."));

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Booking booking = payment.getBooking();
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            eventPublisher.publishEvent(new BookingConfirmedEvent(this, booking));
        }
    }

    @Override
    @Transactional
    public void handleFailedPayment(String transactionId, String errorMessage) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch."));

        // Chỉ xử lý nếu đơn hàng đang ở trạng thái PENDING
        if (payment.getStatus() == PaymentStatus.PENDING) {
            // 1. Cập nhật trạng thái thanh toán thành FAILED
            payment.setStatus(PaymentStatus.FAILED);
            // Có thể lưu thêm errorMessage vào một trường nào đó trong DB nếu có
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // 2. Cập nhật trạng thái Booking thành CANCELLED
            Booking booking = payment.getBooking();
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            // 3. QUAN TRỌNG: Mở lại các khung giờ (TimeSlot) để người khác có thể đặt
            for (TimeSlot slot : booking.getTimeSlots()) {
                slot.setAvailable(true);
                timeSlotRepository.save(slot);
            }
        }
    }

    @Override
    public PaymentResponse getPaymentInfo(Integer bookingId, User user) {
        Booking booking = getBookingAndCheckOwnership(bookingId, user);
        Payment payment = booking.getPayment();

        if (payment == null || payment.getTransactionId() == null) {
            return PaymentResponse.builder()
                    .message("Chưa có link thanh toán cho đơn hàng này.")
                    .build();
        }

        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(payment.getPaymentMethod());
        return strategy.createPaymentRequest(payment.getTransactionId(), payment.getAmount());
    }

    private Booking getBookingAndCheckOwnership(Integer bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!Objects.equals(booking.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xem hoặc thao tác trên đơn hàng này.");
        }
        return booking;
    }
}
