package com.backend.GoPlay.repository;



import com.backend.GoPlay.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {

    /**
     * Tìm tất cả các TimeSlot của một sân cụ thể trong phạm vi một ngày.
     */
    @Query("SELECT ts FROM TimeSlot ts " +
            "WHERE ts.court.id = :courtId " +
            "AND ts.startTime >= :startOfDay " +
            "AND ts.endTime <= :endOfDay " +
            "ORDER BY ts.startTime ASC")
    List<TimeSlot> findAvailableSlotsByCourtAndDate(
            @Param("courtId") Integer courtId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
    // Dùng để kiểm tra TimeSlot đó có tồn tại và còn trống không trước khi Booking
    TimeSlot findByIdAndIsAvailable(Integer id, boolean isAvailable);
}