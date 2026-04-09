package com.bookmyshow.repository;

import com.bookmyshow.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreenId(Long screenId);

    Optional<Seat> findByScreenIdAndSeatNumber(Long screenId, String seatNumber);

    List<Seat> findByScreenIdAndSeatType(Long screenId, Seat.SeatType seatType);

}
