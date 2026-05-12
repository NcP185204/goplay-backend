package com.backend.GoPlay.event;

import com.backend.GoPlay.model.Booking;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class BookingConfirmedEvent extends  ApplicationEvent {
    private final Booking booking;

    public BookingConfirmedEvent(Object source,Booking booking) {
        super(source);
        this.booking = booking;
    }
}
