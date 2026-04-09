package com.bookmyshow.controller;

import com.bookmyshow.dto.BookingConfirmationDTO;
import com.bookmyshow.dto.BookingDTO;
import com.bookmyshow.dto.BookingRequestDTO;
import com.bookmyshow.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Book seats for a show
     * POST /bookings/book
     * 
     * Request body:
     * {
     *   "userId": 1,
     *   "showId": 1,
     *   "seatIds": [1, 2, 3]
     * }
     */
    @PostMapping("/book")
    public ResponseEntity<?> bookSeats(@RequestBody BookingRequestDTO bookingRequest) {
        log.info("POST request to book seats for show {} by user {}", 
                 bookingRequest.getShowId(), bookingRequest.getUserId());
        
        try {
            BookingConfirmationDTO confirmation = bookingService.bookSeats(bookingRequest);
            return new ResponseEntity<>(confirmation, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Invalid booking request: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ErrorResponse("INVALID_REQUEST", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        } catch (IllegalStateException e) {
            log.error("Booking state error: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ErrorResponse("SEAT_UNAVAILABLE", e.getMessage()),
                    HttpStatus.CONFLICT
            );
        } catch (Exception e) {
            log.error("Unexpected error during booking: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ErrorResponse("BOOKING_ERROR", "An unexpected error occurred"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get booking details by ID
     * GET /bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long bookingId) {
        log.info("GET request to fetch booking with id: {}", bookingId);
        BookingDTO booking = bookingService.getBookingById(bookingId);
        if (booking != null) {
            return new ResponseEntity<>(booking, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Get all bookings for a user
     * GET /bookings/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(@PathVariable Long userId) {
        log.info("GET request to fetch bookings for user id: {}", userId);
        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Get all bookings for a show
     * GET /bookings/show/{showId}
     */
    @GetMapping("/show/{showId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByShowId(@PathVariable Long showId) {
        log.info("GET request to fetch bookings for show id: {}", showId);
        List<BookingDTO> bookings = bookingService.getBookingsByShowId(showId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Cancel a booking
     * DELETE /bookings/{bookingId}
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        log.info("DELETE request to cancel booking with id: {}", bookingId);
        
        try {
            BookingDTO booking = bookingService.cancelBooking(bookingId);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Booking not found: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ErrorResponse("NOT_FOUND", e.getMessage()),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    /**
     * Get available seats count for a show
     * GET /bookings/show/{showId}/available-seats
     */
    @GetMapping("/show/{showId}/available-seats")
    public ResponseEntity<?> getAvailableSeats(@PathVariable Long showId) {
        log.info("GET request to fetch available seats for show id: {}", showId);
        
        try {
            Integer availableSeats = bookingService.getAvailableSeatsCount(showId);
            return new ResponseEntity<>(
                    new AvailableSeatsResponse(showId, availableSeats),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            log.error("Error fetching available seats: {}", e.getMessage());
            return new ResponseEntity<>(
                    new ErrorResponse("ERROR", "Could not fetch available seats"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Error response class
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class ErrorResponse {
        private String code;
        private String message;
    }

    /**
     * Available seats response class
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class AvailableSeatsResponse {
        private Long showId;
        private Integer availableSeats;
    }

}
