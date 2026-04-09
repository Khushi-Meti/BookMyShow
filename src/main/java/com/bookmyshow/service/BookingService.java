package com.bookmyshow.service;

import com.bookmyshow.dto.BookingConfirmationDTO;
import com.bookmyshow.dto.BookingDTO;
import com.bookmyshow.dto.BookingRequestDTO;
import com.bookmyshow.model.Booking;
import com.bookmyshow.model.Seat;
import com.bookmyshow.model.Show;
import com.bookmyshow.model.ShowSeat;
import com.bookmyshow.repository.BookingRepository;
import com.bookmyshow.repository.SeatRepository;
import com.bookmyshow.repository.ShowRepository;
import com.bookmyshow.repository.ShowSeatRepository;
import com.bookmyshow.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockService seatLockService;

    /**
     * Book seats for a show with pessimistic write locking and payment processing
     * 
     * Enhanced Concurrency Control & Payment Flow:
     * 1. Create booking record with PENDING status (before lock acquisition)
     * 2. Validate show exists
     * 3. Validate all seats exist and belong to show
     * 4. FETCH SEATS WITH PESSIMISTIC_WRITE LOCK (database-level exclusive lock)
     * 5. CHECK IF ANY SEAT IS ALREADY BOOKED (while holding lock)
     * 6. SIMULATE PAYMENT PROCESSING (random success/failure)
     * 7. IF PAYMENT SUCCESSFUL:
     *    - Mark seats booked
     *    - Update booking status to CONFIRMED
     *    - Return confirmation
     * 8. IF PAYMENT FAILED:
     *    - Update booking status to FAILED
     *    - Release locks (seats remain available)
     *    - Throw payment failure exception
     * 
     * This ensures no race conditions: only one transaction can book these seats at a time.
     * The lock is held during payment processing, guaranteeing consistency.
     */
    public BookingConfirmationDTO bookSeats(BookingRequestDTO bookingRequest) {
        log.info("Booking seats for user {} on show {} with seats: {}", 
                 bookingRequest.getUserId(), bookingRequest.getShowId(), bookingRequest.getSeatIds());

        Long userId = bookingRequest.getUserId();
        Long showId = bookingRequest.getShowId();
        List<Long> seatIds = bookingRequest.getSeatIds();

        // Step 1: Create booking record with PENDING status
        // Persisting BEFORE attempting to acquire locks allows better tracking
        Booking pendingBooking = createPendingBooking(userId, showId);
        log.info("Booking {} created in PENDING status. Will process payment after seat validation.", 
                 pendingBooking.getId());

        try {
            // Step 2: Validate show exists
            Show show = validShowExists(showId);

            // Step 3: Validate all seats exist and belong to the show
            List<Seat> validSeats = validateSeatsForShow(showId, seatIds);
            log.debug("Validated {} seats for show {}", validSeats.size(), showId);

            // Step 4: ACQUIRE PESSIMISTIC WRITE LOCKS on all requested seats
            log.info("Acquiring pessimistic write locks on {} seats for show {}. " +
                    "Only one transaction can proceed from here.", seatIds.size(), showId);
            List<ShowSeat> lockedShowSeats = showSeatRepository.findByShowIdAndSeatIdsWithLock(showId, seatIds);
            log.debug("Pessimistic locks acquired on {} ShowSeat records", lockedShowSeats.size());

            // Step 5: CHECK IF ANY SEAT IS ALREADY BOOKED (while holding pessimistic locks)
            log.debug("Checking booking status of locked seats...");
            checkAndThrowIfAnyBookedSeat(lockedShowSeats);

            // Step 6: SIMULATE PAYMENT PROCESSING
            log.info("Processing payment for booking {} (seats will be locked during payment)...", 
                     pendingBooking.getId());
            PaymentResult paymentResult = simulatePaymentProcessing(userId, seatIds.size());
            
            if (paymentResult.isSuccess()) {
                // Step 7: PAYMENT SUCCESSFUL - Mark seats as booked and confirm booking
                log.info("Payment successful for booking {}. Confirming booking and marking seats.", 
                         pendingBooking.getId());
                
                List<String> bookedSeatNumbers = markSeatsAsBooked(pendingBooking, show, validSeats, 
                                                                    lockedShowSeats, showId);
                
                // Update booking status to CONFIRMED
                updateBookingStatus(pendingBooking.getId(), Booking.BookingStatus.CONFIRMED);
                
                BookingConfirmationDTO confirmation = buildBookingConfirmation(pendingBooking, show, bookedSeatNumbers);
                log.info("Booking {} CONFIRMED. Seats marked as booked.", pendingBooking.getId());
                return confirmation;
                
            } else {
                updateBookingStatus(pendingBooking.getId(), Booking.BookingStatus.FAILED);
                seatLockService.unlockAllSeatsForUser(showId, userId);
                throw new PaymentProcessingException(
                        "Payment processing failed: " + paymentResult.getFailureReason(),
                        pendingBooking.getId()
                );
            }
            
        } catch (PaymentProcessingException ppe) {
            // Payment-specific failure - already handled above
            log.error("Booking {} payment failed", pendingBooking.getId(), ppe);
            throw ppe;
        } catch (IllegalStateException ise) {
            log.error("Booking {} failed due to: {}", pendingBooking.getId(), ise.getMessage());
            updateBookingStatus(pendingBooking.getId(), Booking.BookingStatus.FAILED);
            seatLockService.unlockAllSeatsForUser(showId, userId);
            throw ise;
        } catch (Exception e) {
            log.error("Booking {} failed with unexpected error", pendingBooking.getId(), e);
            updateBookingStatus(pendingBooking.getId(), Booking.BookingStatus.FAILED);
            seatLockService.unlockAllSeatsForUser(showId, userId);
            throw e;
        }
    }

    /**
     * Create a booking record in PENDING status
     * Called BEFORE attempting to acquire locks
     */
    private Booking createPendingBooking(Long userId, Long showId) {
        log.debug("Creating booking in PENDING status for user {} on show {}", userId, showId);
        
        Optional<Show> showOpt = showRepository.findById(showId);
        if (!showOpt.isPresent()) {
            throw new IllegalArgumentException("Show not found with id: " + showId);
        }
        
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShow(showOpt.get());
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setBookingTime(java.time.LocalDateTime.now());
        
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} created with PENDING status", saved.getId());
        return saved;
    }

    /**
     * Validate that show exists in database
     */
    private Show validShowExists(Long showId) {
        Optional<Show> show = showRepository.findById(showId);
        if (!show.isPresent()) {
            log.error("Show not found with id: {}", showId);
            throw new IllegalArgumentException("Show not found with id: " + showId);
        }
        log.debug("Show {} exists", showId);
        return show.get();
    }



    /**
     * Validate that all seat IDs exist and belong to the show's screen
     */
    private List<Seat> validateSeatsForShow(Long showId, List<Long> seatIds) {
        log.debug("Validating {} seats belong to show {}", seatIds.size(), showId);
        
        Show show = showRepository.findById(showId).get();
        Long screenId = show.getScreen().getId();
        
        List<Seat> validSeats = new ArrayList<>();
        for (Long seatId : seatIds) {
            Optional<Seat> seatOpt = seatRepository.findById(seatId);
            if (!seatOpt.isPresent()) {
                log.error("Seat ID {} not found in database", seatId);
                throw new IllegalArgumentException("Seat not found with id: " + seatId);
            }
            
            Seat seat = seatOpt.get();
            if (!seat.getScreen().getId().equals(screenId)) {
                log.error("Seat {} belongs to screen {}, but show {} uses screen {}", 
                         seatId, seat.getScreen().getId(), showId, screenId);
                throw new IllegalArgumentException("Seat " + seatId + " does not belong to this show's screen");
            }
            validSeats.add(seat);
        }
        
        log.debug("All {} seats validated successfully", seatIds.size());
        return validSeats;
    }

    /**
     * Check if any locked ShowSeat is already booked.
     * THIS CHECK HAPPENS WHILE HOLDING PESSIMISTIC LOCKS - seat status cannot change.
     * If any seat is booked, throw exception (lock released by transaction rollback).
     */
    private void checkAndThrowIfAnyBookedSeat(List<ShowSeat> lockedShowSeats) {
        for (ShowSeat lockedSeat : lockedShowSeats) {
            if (lockedSeat.getStatus() == ShowSeat.SeatStatus.BOOKED) {
                throw new IllegalStateException("Seat " + lockedSeat.getSeat().getSeatNumber() + " is already booked");
            }
        }
    }

    /**
     * Create booking record with CONFIRMED status.
     * Happens while holding pessimistic locks.
     */

    /**
     * Mark all seats as booked and link to booking.
     * Still holding pessimistic locks - no other transaction can interfere.
     */
    private List<String> markSeatsAsBooked(Booking booking, Show show, List<Seat> seats,
                                          List<ShowSeat> lockedShowSeats, Long showId) {
        List<String> markedSeats = new ArrayList<>();
        for (Seat seat : seats) {
            ShowSeat showSeat = lockedShowSeats.stream()
                    .filter(ss -> ss.getSeat().getId().equals(seat.getId()))
                    .findFirst().orElse(null);
            if (showSeat == null) {
                showSeat = new ShowSeat();
                showSeat.setShow(show);
                showSeat.setSeat(seat);
            }
            showSeat.setIsBooked(true);
            showSeat.setStatus(ShowSeat.SeatStatus.BOOKED);
            showSeat.setLockedAt(null);
            showSeat.setLockedByUserId(null);
            showSeat.setBooking(booking);
            showSeatRepository.save(showSeat);
            markedSeats.add(seat.getSeatNumber());
        }
        return markedSeats;
    }

    /**
     * Build booking confirmation response
     */
    private BookingConfirmationDTO buildBookingConfirmation(Booking booking, Show show, 
                                                            List<String> seatNumbers) {
        BookingConfirmationDTO conf = new BookingConfirmationDTO();
        conf.setBookingId(booking.getId());
        conf.setUserId(booking.getUserId());
        conf.setShowId(show.getId());
        conf.setMovieName(show.getMovie().getName());
        conf.setShowTime(show.getShowTime());
        conf.setBookedSeats(seatNumbers);
        conf.setStatus(booking.getStatus().toString());
        conf.setBookingTime(booking.getBookingTime());
        conf.setTotalSeats(seatNumbers.size());
        
        return conf;
    }

    /**
     * Cancel booking and mark seats as available
     */
    public BookingDTO cancelBooking(Long bookingId) {
        log.info("Cancelling booking with id: {}", bookingId);
        
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (!bookingOptional.isPresent()) {
            throw new IllegalArgumentException("Booking not found with id: " + bookingId);
        }

        Booking booking = bookingOptional.get();
        
        // Update booking status to CANCELLED
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);

        // Mark all show seats as available
        List<ShowSeat> bookedSeats = showSeatRepository.findByBookingId(bookingId);
        for (ShowSeat showSeat : bookedSeats) {
            showSeat.setIsBooked(false);
            showSeat.setBooking(null);
            showSeatRepository.save(showSeat);
            log.info("Seat {} marked as available for show {}", 
                     showSeat.getSeat().getSeatNumber(), showSeat.getShow().getId());
        }

        return convertEntityToDTO(updatedBooking);
    }

    /**
     * Get booking details
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long bookingId) {
        log.info("Fetching booking with id: {}", bookingId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        return booking.map(this::convertEntityToDTO).orElse(null);
    }

    /**
     * Get all bookings for a user
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        log.info("Fetching bookings for user id: {}", userId);
        return bookingRepository.findByUserId(userId).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for a show
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByShowId(Long showId) {
        log.info("Fetching bookings for show id: {}", showId);
        return bookingRepository.findByShowId(showId).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available seats count for a show
     */
    @Transactional(readOnly = true)
    public Integer getAvailableSeatsCount(Long showId) {
        log.info("Fetching available seats count for show id: {}", showId);
        Optional<Show> show = showRepository.findById(showId);
        if (show.isPresent()) {
            Integer totalSeats = show.get().getScreen().getTotalSeats();
            Integer bookedSeats = showSeatRepository.countByShowIdAndIsBooked(showId, true);
            return totalSeats - bookedSeats;
        }
        return 0;
    }

    private BookingDTO convertEntityToDTO(Booking booking) {
        List<String> seatNumbers = booking.getBookedSeats() == null ? List.of() :
                booking.getBookedSeats().stream()
                        .map(ss -> ss.getSeat().getSeatNumber())
                        .collect(Collectors.toList());

        return new BookingDTO(
                booking.getId(),
                booking.getUserId(),
                booking.getShow().getId(),
                booking.getStatus().toString(),
                booking.getShow().getMovie().getName(),
                booking.getShow().getScreen().getTheatre().getName(),
                booking.getShow().getScreen().getName(),
                booking.getShow().getShowTime(),
                booking.getBookingTime(),
                seatNumbers
        );
    }

    /**
     * Simulate payment processing with 80% success rate
     */
    private PaymentResult simulatePaymentProcessing(Long userId, int seatCount) {
        log.info("Payment processing for user {} with {} seats", userId, seatCount);
        String transactionId = "TXN-" + System.currentTimeMillis() + "-" + userId;
        log.info("Payment successful with transaction ID: {}", transactionId);
        return new PaymentResult(true, null, transactionId);
    }

    /**
     * Update booking status in database
     */
    @Transactional
    private void updateBookingStatus(Long bookingId, Booking.BookingStatus status) {
        log.info("Updating booking {} status to {}", bookingId, status);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isPresent()) {
            Booking b = booking.get();
            Booking.BookingStatus oldStatus = b.getStatus();
            b.setStatus(status);
            bookingRepository.save(b);
            log.info("Booking {} status updated from {} to {}", bookingId, oldStatus, status);
        }
    }

    /**
     * Inner class to hold payment processing results
     */
    private static class PaymentResult {
        private final boolean success;
        private final String failureReason;
        private final String transactionId;

        public PaymentResult(boolean success, String failureReason, String transactionId) {
            this.success = success;
            this.failureReason = failureReason;
            this.transactionId = transactionId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public String getTransactionId() {
            return transactionId;
        }
    }

    /**
     * Custom exception for payment processing failures
     */
    public static class PaymentProcessingException extends RuntimeException {
        private final Long bookingId;

        public PaymentProcessingException(String message, Long bookingId) {
            super(message);
            this.bookingId = bookingId;
        }

        public Long getBookingId() {
            return bookingId;
        }
    }

}
