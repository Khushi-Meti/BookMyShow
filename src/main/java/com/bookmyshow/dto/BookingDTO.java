package com.bookmyshow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private Long id;
    private Long userId;
    private Long showId;
    private String status;
    private String movieName;
    private String theatreName;
    private String screenName;
    private LocalDateTime showTime;
    private LocalDateTime bookingTime;
    private List<String> seats;

}
