package com.backend.GoPlay.service;

import com.backend.GoPlay.event.BookingConfirmedEvent;

public interface NotificationService {

    void handleBookingConfirmedEvent(BookingConfirmedEvent event);

}