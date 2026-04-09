package com.bookmyshow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class BookMyShowApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookMyShowApplication.class, args);
    }

}
