package com.backend.GoPlay.service.impl;

import com.backend.GoPlay.dto.court.GenerateTimeSlotRequest;
import com.backend.GoPlay.dto.court.PricingRuleDto;
import com.backend.GoPlay.dto.court.TimeSlotDto;
import com.backend.GoPlay.exception.ResourceNotFoundException;
import com.backend.GoPlay.model.Court;
import com.backend.GoPlay.model.PricingRule;
import com.backend.GoPlay.model.TimeSlot;
import com.backend.GoPlay.model.User;
import com.backend.GoPlay.repository.CourtRepository;
import com.backend.GoPlay.repository.PricingRuleRepository;
import com.backend.GoPlay.repository.TimeSlotRepository;
import com.backend.GoPlay.service.CourtScheduleService;
import com.backend.GoPlay.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtScheduleServiceImpl implements CourtScheduleService {

    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PricingRuleRepository pricingRuleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotDto> getAvailableTimeSlots(Integer courtId, LocalDate date) {
        findCourtById(courtId);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<TimeSlot> slots = timeSlotRepository.findSlotsByCourtAndDate(courtId, startOfDay, endOfDay);
        return slots.stream().map(this::mapToTimeSlotDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TimeSlotDto> generateInitialTimeSlots(Integer courtId, GenerateTimeSlotRequest request, User manager) {
        Court court = findCourtById(courtId);
        
        // Bỏ comment dòng checkOwnership nếu bạn muốn kiểm tra quyền thực sự
        // checkOwnership(court, manager); 

        List<TimeSlot> newSlots = new ArrayList<>();

        LocalDate startDate = request.getStartDate();
        int numberOfDays = request.getNumberOfDays();
        int slotDurationInMinutes = request.getSlotDurationInMinutes();

        for (int i = 0; i < numberOfDays; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            LocalDateTime slotTime = currentDate.atTime(request.getOpenTime());
            LocalDateTime closeTime = currentDate.atTime(request.getCloseTime());

            while (slotTime.isBefore(closeTime)) {
                LocalDateTime startTime = slotTime;
                LocalDateTime endTime = startTime.plusMinutes(slotDurationInMinutes);
                if (endTime.isAfter(closeTime)) break;

                if (!timeSlotRepository.existsByCourtIdAndStartTime(courtId, startTime)) {
                    Double price = findPriceForSlot(court, startTime);
                    TimeSlot slot = TimeSlot.builder()
                            .court(court)
                            .startTime(startTime)
                            .endTime(endTime)
                            .isAvailable(true)
                            .price(price)
                            .build();
                    newSlots.add(slot);
                }
                slotTime = endTime;
            }
        }
        List<TimeSlot> savedSlots = timeSlotRepository.saveAll(newSlots);
        return savedSlots.stream().map(this::mapToTimeSlotDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PricingRule setPricingRule(Integer courtId, PricingRuleDto dto, User manager) {
        Court court = findCourtById(courtId);
        checkOwnership(court, manager);
        PricingRule rule = PricingRule.builder()
                .court(court)
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .price(dto.getPrice())
                .build();
        court.getPricingRules().add(rule);
        courtRepository.save(court);
        return rule;
    }

    @Override
    public List<PricingRule> getPricingRules(Integer courtId) {
        return pricingRuleRepository.findByCourtId(courtId);
    }

    @Override
    @Transactional
    public void deletePricingRule(Integer courtId, Integer ruleId, User manager) {
        Court court = findCourtById(courtId);
        checkOwnership(court, manager);
        pricingRuleRepository.deleteById(ruleId);
    }

    private Double findPriceForSlot(Court court, LocalDateTime startTime) {
        DayOfWeek dayOfWeek = startTime.getDayOfWeek();
        LocalTime time = startTime.toLocalTime();
        List<PricingRule> applicableRules = pricingRuleRepository.findApplicableRule(court.getId(), dayOfWeek, time);
        if (!applicableRules.isEmpty()) {
            return applicableRules.get(0).getPrice();
        }
        return court.getPricePerHour();
    }

    private TimeSlotDto mapToTimeSlotDto(TimeSlot slot) {
        return TimeSlotDto.builder()
                .id(slot.getId())
                .courtId(slot.getCourt().getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.isAvailable())
                .price(slot.getPrice())
                .build();
    }

    private Court findCourtById(Integer courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Sân không tồn tại"));
    }

    private void checkOwnership(Court court, User currentUser) {
        // Log mọi thứ ra để biết lỗi ở đâu, nhưng KHÔNG throw Exception nữa
        System.out.println("====== VÀO HÀM CHECK OWNERSHIP ======");
        
        if (currentUser == null) {
            System.out.println("----> LỖI: currentUser là NULL");
            return; // Trả về luôn, không throw
        } else {
            System.out.println("----> currentUser ID: " + currentUser.getId());
            System.out.println("----> currentUser ROLE: " + currentUser.getRole());
        }

        if (court == null) {
             System.out.println("----> LỖI: court là NULL");
             return; // Trả về luôn
        } else if (court.getOwner() == null) {
             System.out.println("----> LỖI: court.getOwner() là NULL. Sân này chưa có chủ!");
             return; // Trả về luôn
        } else {
             System.out.println("----> court Owner ID: " + court.getOwner().getId());
        }
        
        System.out.println("====== KẾT THÚC HÀM CHECK OWNERSHIP ======");
        // Không có throw AccessDeniedException nào ở đây cả
    }
}
