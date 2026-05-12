package com.backend.GoPlay.service;

import com.backend.GoPlay.dto.court.GenerateTimeSlotRequest;
import com.backend.GoPlay.dto.court.PricingRuleDto;
import com.backend.GoPlay.dto.court.TimeSlotDto;
import com.backend.GoPlay.model.PricingRule;
import com.backend.GoPlay.model.User;

import java.time.LocalDate;
import java.util.List;

public interface CourtScheduleService {
    // TimeSlot methods
    List<TimeSlotDto> getAvailableTimeSlots(Integer courtId, LocalDate date);
    List<TimeSlotDto> generateInitialTimeSlots(Integer courtId, GenerateTimeSlotRequest request, User manager);

    // PricingRule methods
    PricingRule setPricingRule(Integer courtId, PricingRuleDto dto, User manager);
    List<PricingRule> getPricingRules(Integer courtId);
    void deletePricingRule(Integer courtId, Integer ruleId, User manager);
}
