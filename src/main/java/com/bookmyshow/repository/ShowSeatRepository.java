package com.bookmyshow.repository;

import com.bookmyshow.model.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.id = :seatId")
    Optional<ShowSeat> findByShowIdAndSeatId(@Param("showId") Long showId, @Param("seatId") Long seatId);

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findBySeatId(Long seatId);

    List<ShowSeat> findByShowIdAndIsBooked(Long showId, Boolean isBooked);

    List<ShowSeat> findByBookingId(Long bookingId);

    Integer countByShowIdAndIsBooked(Long showId, Boolean isBooked);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.id IN (:seatIds)")
    List<ShowSeat> findByShowIdAndSeatIdsWithLock(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.id = :seatId")
    Optional<ShowSeat> findByShowIdAndSeatIdWithLock(@Param("showId") Long showId, @Param("seatId") Long seatId);

    // Find expired locks (locked more than 5 minutes ago)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.status = 'LOCKED' AND ss.lockedAt < :expiry")
    List<ShowSeat> findExpiredLocks(@Param("expiry") LocalDateTime expiry);

    // Find seats locked by a specific user for a show
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.lockedByUserId = :userId AND ss.status = 'LOCKED'")
    List<ShowSeat> findLockedByUserAndShow(@Param("showId") Long showId, @Param("userId") Long userId);

}
