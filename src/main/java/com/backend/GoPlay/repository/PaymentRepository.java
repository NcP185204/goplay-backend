package com.backend.GoPlay.repository;

import com.backend.GoPlay.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Tìm thanh toán bằng mã giao dịch của cổng thanh toán
    Optional<Payment> findByTransactionId(String transactionId);
}