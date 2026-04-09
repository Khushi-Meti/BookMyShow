package com.bookmyshow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmationDTO {

    private Long bookingId;
    private Long userId;
    private Long showId;
    private String movieName;
    private LocalDateTime showTime;
    private List<String> bookedSeats;
    private String status;
    private LocalDateTime bookingTime;
    private Integer totalSeats;

}
