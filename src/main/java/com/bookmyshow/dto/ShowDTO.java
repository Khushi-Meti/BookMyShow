package com.bookmyshow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDTO {

    private Long id;
    private LocalDateTime showTime;
    private Long movieId;
    private Long screenId;
    private ScreenDTO screen;


}

