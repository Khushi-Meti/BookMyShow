package com.bookmyshow.repository;

import com.bookmyshow.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT DISTINCT b FROM Booking b JOIN FETCH b.show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theatre LEFT JOIN FETCH b.bookedSeats bs LEFT JOIN FETCH bs.seat WHERE b.userId = :userId AND b.status = 'CONFIRMED'")
    List<Booking> findByUserId(@Param("userId") Long userId);

    List<Booking> findByShowId(Long showId);

    List<Booking> findByStatus(Booking.BookingStatus status);

    List<Booking> findByUserIdAndStatus(Long userId, Booking.BookingStatus status);

    List<Booking> findByBookingTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Booking> findByShowIdAndStatus(Long showId, Booking.BookingStatus status);

}
