package com.bookmyshow.service;

import com.bookmyshow.dto.BookingRequestDTO;
import com.bookmyshow.model.*;
import com.bookmyshow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency test for BookingService.
 * 
 * Tests that pessimistic locking correctly prevents race conditions
 * when multiple threads try to book the same seat simultaneously.
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingServiceConcurrencyTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private TheatreRepository theatreRepository;
    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ShowSeatRepository showSeatRepository;

    private Long showId;
    private Long seatId;
    private static final int CONCURRENT_THREADS = 10;

    @BeforeEach
    @Transactional
    void setUp() {
        // Create test data: Movie -> Theatre -> Screen -> Show -> Seats
        // Use unique names to avoid constraint violations across test runs
        String uniqueSuffix = " " + UUID.randomUUID();
        
        // 1. Create Movie
        Movie movie = new Movie();
        movie.setName("Concurrency Test Movie" + uniqueSuffix);
        movie.setDuration(120);
        movie.setLanguage("English");
        movie = movieRepository.save(movie);

        // 2. Create Theatre
        Theatre theatre = new Theatre();
        theatre.setName("Test Theatre" + uniqueSuffix);
        theatre.setLocation("Test Location");
        theatre = theatreRepository.save(theatre);

        // 3. Create Screen
        Screen screen = new Screen();
        screen.setName("Screen 1" + uniqueSuffix);
        screen.setTotalSeats(50);
        screen.setTheatre(theatre);
        screen = screenRepository.save(screen);

        // 4. Create Seats for the screen
        for (int i = 1; i <= 50; i++) {
            Seat seat = new Seat();
            seat.setScreen(screen);
            seat.setSeatNumber("A" + i);
            seat.setSeatType(Seat.SeatType.STANDARD);
            seatRepository.save(seat);
        }

        // 5. Create Show
        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowTime(LocalDateTime.now().plusHours(2));
        show = showRepository.save(show);

        // 6. Create ShowSeats for all seats
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        for (Seat seat : seats) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(show);
            showSeat.setSeat(seat);
            showSeat.setIsBooked(false);
            showSeatRepository.save(showSeat);
        }

        // Use first seat for concurrency test
        this.seatId = seats.get(0).getId();
        this.showId = show.getId();
    }

    /**
     * Test multiple threads trying to book the same seat concurrently.
     * 
     * Expected behavior:
     * - 10 threads try to book the same seat within the same transaction window
     * - Pessimistic locking ensures only ONE thread can acquire the lock and complete the booking
     * - Other 9 threads should fail with IllegalStateException (seat already booked)
     *   OR PaymentProcessingException (if they lost the lock race and payment failed)
     * 
     * Verified outcomes:
     * - Exactly 1 booking succeeds (CONFIRMED status)
     * - 9 bookings fail with appropriate exceptions
     * - No double-booking occurs
     * - Seat remains booked only once
     */
    @Test
    void testConcurrentBookingOfSameSeat() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        
        // Track results from each thread
        List<BookingResult> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        
        // Submit 10 concurrent booking requests for the same seat
        for (int threadId = 1; threadId <= CONCURRENT_THREADS; threadId++) {
            final int userId = 100 + threadId; // Users: 101-110
            
            executorService.submit(() -> {
                try {
                    BookingRequestDTO request = new BookingRequestDTO();
                    request.setUserId((long) userId);
                    request.setShowId(showId);
                    request.setSeatIds(List.of(seatId));
                    
                    // Try to book the same seat
                    var confirmation = bookingService.bookSeats(request);
                    
                    results.add(new BookingResult(userId, true, null, confirmation.getBookingId()));
                    
                } catch (BookingService.PaymentProcessingException ppe) {
                    // Payment failed - one of the expected failure modes
                    results.add(new BookingResult(userId, false, "PaymentFailed", null));
                    
                } catch (IllegalStateException ise) {
                    // Seat already booked - expected when pessimistic lock acquired by another thread first
                    results.add(new BookingResult(userId, false, "SeatAlreadyBooked", null));
                    
                } catch (Exception e) {
                    // Unexpected exception
                    results.add(new BookingResult(userId, false, "UnexpectedException: " + e.getClass().getSimpleName(), null));
                    
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete (max 30 seconds)
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "All threads should complete within timeout");
        
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS), 
                   "ExecutorService should terminate");
        
        // Verify results
        assertEquals(CONCURRENT_THREADS, results.size(), 
                     "All 10 threads should have completed");
        
        // Count successful and failed bookings
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        // Verify concurrency control worked: ONE success + NINE failures (either payment failed or seat already booked)
        assertEquals(1, successCount, 
                     "Exactly ONE booking should succeed due to pessimistic locking (first to acquire lock)");
        assertEquals(CONCURRENT_THREADS - 1, failureCount, 
                     "Remaining 9 bookings should fail (either payment failure or seat already booked)");
        
        // Get the successful booking
        BookingResult successfulBooking = results.stream()
                .filter(r -> r.success)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Must have exactly one successful booking"));
        
        // Verify the successful booking is CONFIRMED in database
        Booking confirmedBooking = bookingRepository.findById(successfulBooking.bookingId)
                .orElseThrow();
        assertEquals(Booking.BookingStatus.CONFIRMED, confirmedBooking.getStatus(),
                     "Successful booking should have CONFIRMED status");
        
        // Verify the seat is marked as booked only once
        ShowSeat bookedShowSeat = showSeatRepository.findByShowIdAndSeatId(showId, seatId)
                .orElseThrow();
        assertTrue(bookedShowSeat.getIsBooked(),
                   "Seat should be marked as booked");
        assertEquals(confirmedBooking.getId(), bookedShowSeat.getBooking().getId(),
                     "Seat should be linked to the successful booking");
        
        // Verify no other booking is booked for that seat
        long bookedCountForSeat = showSeatRepository.countByShowIdAndIsBooked(showId, true);
        assertEquals(1, bookedCountForSeat,
                     "Only one booking for this seat should exist in database");
        
        // Print results for verification
        System.out.println("\n========== CONCURRENCY TEST RESULTS ==========");
        System.out.println("Successful booking:");
        System.out.println("  User ID: " + successfulBooking.userId);
        System.out.println("  Booking ID: " + successfulBooking.bookingId);
        System.out.println();
        System.out.println("Failed bookings (" + failureCount + " total):");
        Map<String, Long> failureReasonCounts = new HashMap<>();
        results.stream()
               .filter(r -> !r.success)
               .forEach(r -> {
                   failureReasonCounts.merge(r.failureReason, 1L, Long::sum);
                   System.out.println("  User " + r.userId + ": " + r.failureReason);
               });
        System.out.println("\nFailure Summary:");
        failureReasonCounts.forEach((reason, count) -> 
            System.out.println("  " + reason + ": " + count)
        );
        System.out.println("===========================================\n");
    }

    /**
     * Test concurrent booking of different seats by multiple threads.
     * This verifies that pessimistic locking doesn't prevent legitimate concurrent bookings of DIFFERENT seats.
     */
    @Test
    void testConcurrentBookingOfDifferentSeats() throws InterruptedException {
        // Get first 10 seats for this test
        List<Seat> seats = seatRepository.findByScreenId(
            showRepository.findById(showId).orElseThrow().getScreen().getId()
        );
        seats = seats.subList(0, Math.min(10, seats.size()));
        
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        List<BookingResult> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        
        // Each thread books a DIFFERENT seat
        for (int threadId = 0; threadId < Math.min(CONCURRENT_THREADS, seats.size()); threadId++) {
            final int userId = 200 + threadId;
            final Long seatIdForThread = seats.get(threadId).getId();
            
            executorService.submit(() -> {
                try {
                    BookingRequestDTO request = new BookingRequestDTO();
                    request.setUserId((long) userId);
                    request.setShowId(showId);
                    request.setSeatIds(List.of(seatIdForThread));
                    
                    var confirmation = bookingService.bookSeats(request);
                    results.add(new BookingResult(userId, true, null, confirmation.getBookingId()));
                    
                } catch (Exception e) {
                    results.add(new BookingResult(userId, false, e.getMessage(), null));
                    
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "All threads should complete");
        
        executorService.shutdown();
        
        // When booking different seats, ALL should succeed (or mostly succeed due to payment simulation)
        long successCount = results.stream().filter(r -> r.success).count();
        assertTrue(successCount > 0, 
                   "At least some bookings for different seats should succeed");
        
        System.out.println("\n========== DIFFERENT SEATS TEST RESULTS ==========");
        System.out.println("Total bookings: " + results.size());
        System.out.println("Successful: " + successCount);
        System.out.println("Failed: " + results.stream().filter(r -> !r.success).count());
        results.stream()
               .filter(r -> !r.success)
               .forEach(r -> System.out.println("  Failed - User " + r.userId + ": " + r.failureReason));
        System.out.println("================================================\n");
    }

    /**
     * Helper class to track booking results from concurrent threads
     */
    private static class BookingResult {
        int userId;
        boolean success;
        String failureReason;
        Long bookingId;

        BookingResult(int userId, boolean success, String failureReason, Long bookingId) {
            this.userId = userId;
            this.success = success;
            this.failureReason = failureReason;
            this.bookingId = bookingId;
        }
    }
}
