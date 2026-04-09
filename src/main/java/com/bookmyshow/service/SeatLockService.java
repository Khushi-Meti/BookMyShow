package com.bookmyshow.service;

import com.bookmyshow.model.ShowSeat;
import com.bookmyshow.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockService {

    private static final int LOCK_DURATION_MINUTES = 5;

    private final ShowSeatRepository showSeatRepository;

    /**
     * Lock seats for a user. Uses pessimistic write lock to prevent race conditions.
     * Throws if any seat is already LOCKED by another user or BOOKED.
     */
    @Transactional
    public void lockSeats(Long showId, List<Long> seatIds, Long userId) {
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdsWithLock(showId, seatIds);

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() == ShowSeat.SeatStatus.BOOKED) {
                throw new IllegalStateException("Seat " + ss.getSeat().getSeatNumber() + " is already booked");
            }
            if (ss.getStatus() == ShowSeat.SeatStatus.LOCKED && !ss.getLockedByUserId().equals(userId)) {
                throw new IllegalStateException("Seat " + ss.getSeat().getSeatNumber() + " is locked by another user");
            }
            ss.setStatus(ShowSeat.SeatStatus.LOCKED);
            ss.setLockedAt(LocalDateTime.now());
            ss.setLockedByUserId(userId);
            showSeatRepository.save(ss);
        }

        log.info("Locked {} seats for user {} on show {}", seatIds.size(), userId, showId);
    }

    /**
     * Unlock specific seats for a user (on deselect or payment failure).
     */
    @Transactional
    public void unlockSeats(Long showId, List<Long> seatIds, Long userId) {
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdsWithLock(showId, seatIds);

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() == ShowSeat.SeatStatus.LOCKED && userId.equals(ss.getLockedByUserId())) {
                ss.setStatus(ShowSeat.SeatStatus.AVAILABLE);
                ss.setLockedAt(null);
                ss.setLockedByUserId(null);
                showSeatRepository.save(ss);
            }
        }

        log.info("Unlocked {} seats for user {} on show {}", seatIds.size(), userId, showId);
    }

    /**
     * Unlock all seats held by a user for a show (called on payment failure).
     */
    @Transactional
    public void unlockAllSeatsForUser(Long showId, Long userId) {
        List<ShowSeat> locked = showSeatRepository.findLockedByUserAndShow(showId, userId);
        for (ShowSeat ss : locked) {
            ss.setStatus(ShowSeat.SeatStatus.AVAILABLE);
            ss.setLockedAt(null);
            ss.setLockedByUserId(null);
            showSeatRepository.save(ss);
        }
        log.info("Unlocked all seats for user {} on show {}", userId, showId);
    }

    /**
     * Release all expired locks (older than 5 minutes). Called by scheduler.
     */
    @Transactional
    public void releaseExpiredLocks() {
        LocalDateTime expiry = LocalDateTime.now().minusMinutes(LOCK_DURATION_MINUTES);
        List<ShowSeat> expired = showSeatRepository.findExpiredLocks(expiry);

        for (ShowSeat ss : expired) {
            ss.setStatus(ShowSeat.SeatStatus.AVAILABLE);
            ss.setLockedAt(null);
            ss.setLockedByUserId(null);
            showSeatRepository.save(ss);
        }

        if (!expired.isEmpty()) {
            log.info("Released {} expired seat locks", expired.size());
        }
    }

    public static int getLockDurationMinutes() {
        return LOCK_DURATION_MINUTES;
    }
}
