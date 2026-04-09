package com.bookmyshow.controller;

import com.bookmyshow.dto.SeatDTO;
import com.bookmyshow.model.ShowSeat;
import com.bookmyshow.repository.SeatRepository;
import com.bookmyshow.repository.ShowSeatRepository;
import com.bookmyshow.service.SeatLockService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SeatController {

    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockService seatLockService;

    // GET /api/seats/screen/{screenId}/show/{showId}?userId={userId}
    @GetMapping("/screen/{screenId}/show/{showId}")
    public ResponseEntity<List<SeatDTO>> getSeatsByScreenAndShow(
            @PathVariable Long screenId, @PathVariable Long showId,
            @RequestParam(required = false) Long userId) {
        log.info("GET seats for screen {} show {} user {}", screenId, showId, userId);

        Map<Long, ShowSeat> showSeatMap = showSeatRepository.findByShowId(showId)
                .stream()
                .collect(Collectors.toMap(ss -> ss.getSeat().getId(), ss -> ss));

        List<SeatDTO> seats = seatRepository.findByScreenId(screenId).stream()
                .map(s -> {
                    ShowSeat ss = showSeatMap.get(s.getId());
                    String status;
                    if (ss == null) {
                        status = "AVAILABLE";
                    } else if (ss.getStatus() == ShowSeat.SeatStatus.BOOKED) {
                        status = "BOOKED";
                    } else if (ss.getStatus() == ShowSeat.SeatStatus.LOCKED) {
                        // Show as LOCKED_BY_ME if this user locked it, so frontend renders it selected
                        status = (userId != null && userId.equals(ss.getLockedByUserId()))
                                ? "LOCKED_BY_ME" : "LOCKED";
                    } else {
                        status = "AVAILABLE";
                    }
                    return new SeatDTO(s.getId(), s.getSeatNumber(), s.getSeatType().name(), status);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(seats);
    }

    // POST /api/seats/lock  — lock a seat when user selects it
    @PostMapping("/lock")
    public ResponseEntity<?> lockSeat(@RequestBody SeatLockRequest req) {
        log.info("Lock request: show={} seat={} user={}", req.getShowId(), req.getSeatId(), req.getUserId());
        try {
            seatLockService.lockSeats(req.getShowId(), List.of(req.getSeatId()), req.getUserId());
            return ResponseEntity.ok(Map.of("message", "Seat locked for 5 minutes"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/seats/lock  — unlock a seat when user deselects it
    @DeleteMapping("/lock")
    public ResponseEntity<?> unlockSeat(@RequestBody SeatLockRequest req) {
        log.info("Unlock request: show={} seat={} user={}", req.getShowId(), req.getSeatId(), req.getUserId());
        seatLockService.unlockSeats(req.getShowId(), List.of(req.getSeatId()), req.getUserId());
        return ResponseEntity.ok(Map.of("message", "Seat unlocked"));
    }

    @Data
    static class SeatLockRequest {
        private Long showId;
        private Long seatId;
        private Long userId;
    }
}
