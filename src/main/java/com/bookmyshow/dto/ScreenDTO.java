package com.bookmyshow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenDTO {

    private Long id;
    private String name;
    private Integer totalSeats;
    private TheatreDTO theatre;

}
