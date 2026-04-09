package com.bookmyshow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatLockCleanupScheduler {

    private final SeatLockService seatLockService;

    // Runs every 30 seconds to release expired locks
    @Scheduled(fixedDelay = 30000)
    public void releaseExpiredLocks() {
        seatLockService.releaseExpiredLocks();
    }
}
