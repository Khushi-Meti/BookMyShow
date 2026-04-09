package com.bookmyshow.init;

import com.bookmyshow.model.*;
import com.bookmyshow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Clearing existing data and initializing database with dummy data...");

        // Clear existing data in reverse order of dependencies
        showSeatRepository.deleteAll();
        showRepository.deleteAll();
        seatRepository.deleteAll();
        screenRepository.deleteAll();
        theatreRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();

        log.info("Existing data cleared. Initializing with new dummy data...");

        // Create Movies
        Movie movie1 = new Movie();
        movie1.setName("Avengers: Endgame");
        movie1.setDuration(180);
        movie1.setLanguage("English");

        Movie movie2 = new Movie();
        movie2.setName("Pushpa: The Rise");
        movie2.setDuration(179);
        movie2.setLanguage("Telugu");

        Movie movie3 = new Movie();
        movie3.setName("Oppenheimer");
        movie3.setDuration(180);
        movie3.setLanguage("English");

        Movie movie4 = new Movie();
        movie4.setName("Barbie");
        movie4.setDuration(114);
        movie4.setLanguage("English");

        Movie movie5 = new Movie();
        movie5.setName("Dune: Part Two");
        movie5.setDuration(166);
        movie5.setLanguage("English");

        Movie movie6 = new Movie();
        movie6.setName("RRR");
        movie6.setDuration(187);
        movie6.setLanguage("Telugu");

        Movie movie7 = new Movie();
        movie7.setName("Kantara");
        movie7.setDuration(148);
        movie7.setLanguage("Kannada");

        movieRepository.saveAll(List.of(movie1, movie2, movie3, movie4, movie5, movie6, movie7));
        log.info("✓ Created 7 movies");

        // Create Theatres
        Theatre theatre1 = new Theatre();
        theatre1.setName("PVR Cinemas");
        theatre1.setLocation("Hyderabad");

        Theatre theatre2 = new Theatre();
        theatre2.setName("IMAX Theatre");
        theatre2.setLocation("Mumbai");

        Theatre theatre3 = new Theatre();
        theatre3.setName("Cinepolis");
        theatre3.setLocation("Bangalore");

        Theatre theatre4 = new Theatre();
        theatre4.setName("INOX Movies");
        theatre4.setLocation("Hyderabad");

        Theatre theatre5 = new Theatre();
        theatre5.setName("PVR Director's Cut");
        theatre5.setLocation("Mumbai");

        Theatre theatre6 = new Theatre();
        theatre6.setName("Fun Cinemas");
        theatre6.setLocation("Bangalore");

        // New theatres
        Theatre theatre7 = new Theatre();
        theatre7.setName("Central Mall");
        theatre7.setLocation("Bangalore");

        Theatre theatre8 = new Theatre();
        theatre8.setName("Lulu Mall");
        theatre8.setLocation("Bangalore");

        Theatre theatre9 = new Theatre();
        theatre9.setName("PVR Mall");
        theatre9.setLocation("Hyderabad");

        Theatre theatre10 = new Theatre();
        theatre10.setName("Inox Mall");
        theatre10.setLocation("Hyderabad");

        Theatre theatre11 = new Theatre();
        theatre11.setName("Miraj Cinemas");
        theatre11.setLocation("Mumbai");

        theatreRepository.saveAll(List.of(theatre1, theatre2, theatre3, theatre4, theatre5, theatre6,
                theatre7, theatre8, theatre9, theatre10, theatre11));
        log.info("✓ Created 11 theatres");

        theatre1 = theatreRepository.findById(theatre1.getId()).orElse(theatre1);
        theatre2 = theatreRepository.findById(theatre2.getId()).orElse(theatre2);
        theatre3 = theatreRepository.findById(theatre3.getId()).orElse(theatre3);
        theatre4 = theatreRepository.findById(theatre4.getId()).orElse(theatre4);
        theatre5 = theatreRepository.findById(theatre5.getId()).orElse(theatre5);
        theatre6 = theatreRepository.findById(theatre6.getId()).orElse(theatre6);
        theatre7 = theatreRepository.findById(theatre7.getId()).orElse(theatre7);
        theatre8 = theatreRepository.findById(theatre8.getId()).orElse(theatre8);
        theatre9 = theatreRepository.findById(theatre9.getId()).orElse(theatre9);
        theatre10 = theatreRepository.findById(theatre10.getId()).orElse(theatre10);
        theatre11 = theatreRepository.findById(theatre11.getId()).orElse(theatre11);

        // Create Screens for each Theatre
        Screen screen1 = new Screen();
        screen1.setName("Screen 1 (IMAX)");
        screen1.setTotalSeats(150);
        screen1.setTheatre(theatre1);

        Screen screen2 = new Screen();
        screen2.setName("Screen 2 (4DX)");
        screen2.setTotalSeats(120);
        screen2.setTheatre(theatre1);

        Screen screen3 = new Screen();
        screen3.setName("Screen 1 (Premium)");
        screen3.setTotalSeats(100);
        screen3.setTheatre(theatre2);

        Screen screen4 = new Screen();
        screen4.setName("Screen 2 (Standard)");
        screen4.setTotalSeats(200);
        screen4.setTheatre(theatre3);

        Screen screen5 = new Screen();
        screen5.setName("Screen 1 (Dolby Atmos)");
        screen5.setTotalSeats(180);
        screen5.setTheatre(theatre4);

        Screen screen6 = new Screen();
        screen6.setName("Screen 1 (VIP)");
        screen6.setTotalSeats(80);
        screen6.setTheatre(theatre5);

        Screen screen7 = new Screen();
        screen7.setName("Screen 1 (3D)");
        screen7.setTotalSeats(160);
        screen7.setTheatre(theatre6);

        Screen screen8 = new Screen();
        screen8.setName("Screen 1 (4K)");
        screen8.setTotalSeats(140);
        screen8.setTheatre(theatre7);

        Screen screen9 = new Screen();
        screen9.setName("Screen 1 (Standard)");
        screen9.setTotalSeats(120);
        screen9.setTheatre(theatre8);

        Screen screen10 = new Screen();
        screen10.setName("Screen 1 (IMAX)");
        screen10.setTotalSeats(150);
        screen10.setTheatre(theatre9);

        Screen screen11 = new Screen();
        screen11.setName("Screen 1 (Dolby)");
        screen11.setTotalSeats(130);
        screen11.setTheatre(theatre10);

        Screen screen12 = new Screen();
        screen12.setName("Screen 1 (Gold)");
        screen12.setTotalSeats(100);
        screen12.setTheatre(theatre11);

        screenRepository.saveAll(List.of(screen1, screen2, screen3, screen4, screen5, screen6,
                screen7, screen8, screen9, screen10, screen11, screen12));
        log.info("✓ Created 12 screens");

        createSeatsForScreen(screen1, 150);
        createSeatsForScreen(screen2, 120);
        createSeatsForScreen(screen3, 100);
        createSeatsForScreen(screen4, 200);
        createSeatsForScreen(screen5, 180);
        createSeatsForScreen(screen6, 80);
        createSeatsForScreen(screen7, 160);
        createSeatsForScreen(screen8, 140);
        createSeatsForScreen(screen9, 120);
        createSeatsForScreen(screen10, 150);
        createSeatsForScreen(screen11, 130);
        createSeatsForScreen(screen12, 100);
        log.info("✓ Created seats for all screens");

        // Refresh movies and screens to get the generated IDs
        movie1 = movieRepository.findById(movie1.getId()).orElse(movie1);
        movie2 = movieRepository.findById(movie2.getId()).orElse(movie2);
        movie3 = movieRepository.findById(movie3.getId()).orElse(movie3);
        movie4 = movieRepository.findById(movie4.getId()).orElse(movie4);
        movie5 = movieRepository.findById(movie5.getId()).orElse(movie5);
        movie6 = movieRepository.findById(movie6.getId()).orElse(movie6);
        movie7 = movieRepository.findById(movie7.getId()).orElse(movie7);
        screen1 = screenRepository.findById(screen1.getId()).orElse(screen1);
        screen2 = screenRepository.findById(screen2.getId()).orElse(screen2);
        screen3 = screenRepository.findById(screen3.getId()).orElse(screen3);
        screen4 = screenRepository.findById(screen4.getId()).orElse(screen4);
        screen5 = screenRepository.findById(screen5.getId()).orElse(screen5);
        screen6 = screenRepository.findById(screen6.getId()).orElse(screen6);
        screen7 = screenRepository.findById(screen7.getId()).orElse(screen7);
        screen8 = screenRepository.findById(screen8.getId()).orElse(screen8);
        screen9 = screenRepository.findById(screen9.getId()).orElse(screen9);
        screen10 = screenRepository.findById(screen10.getId()).orElse(screen10);
        screen11 = screenRepository.findById(screen11.getId()).orElse(screen11);
        screen12 = screenRepository.findById(screen12.getId()).orElse(screen12);

        // Create Shows
        // ── Hyderabad: PVR (screen1, screen2) + INOX (screen5) ──
        Show show1 = new Show(); // Avengers @ PVR IMAX Hyderabad
        show1.setMovie(movie1);
        show1.setScreen(screen1);
        show1.setShowTime(LocalDateTime.now().plusHours(2));

        Show show5 = new Show(); // Avengers @ PVR 4DX Hyderabad (evening)
        show5.setMovie(movie1);
        show5.setScreen(screen2);
        show5.setShowTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));

        Show show7 = new Show(); // Dune @ INOX Dolby Hyderabad
        show7.setMovie(movie5);
        show7.setScreen(screen5);
        show7.setShowTime(LocalDateTime.now().plusHours(4));

        Show showHyd4 = new Show(); // RRR @ INOX Dolby Hyderabad
        showHyd4.setMovie(movie6);
        showHyd4.setScreen(screen5);
        showHyd4.setShowTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0));

        // ── Mumbai: IMAX (screen3) + PVR Director's Cut (screen6) ──
        Show show3 = new Show(); // Oppenheimer @ IMAX Mumbai
        show3.setMovie(movie3);
        show3.setScreen(screen3);
        show3.setShowTime(LocalDateTime.now().plusHours(3));

        Show show8 = new Show(); // RRR @ PVR Director's Cut Mumbai
        show8.setMovie(movie6);
        show8.setScreen(screen6);
        show8.setShowTime(LocalDateTime.now().plusHours(6));

        Show showMum4 = new Show(); // Dune @ PVR Director's Cut Mumbai
        showMum4.setMovie(movie5);
        showMum4.setScreen(screen6);
        showMum4.setShowTime(LocalDateTime.now().plusDays(1).withHour(13).withMinute(0));

        // ── Bangalore: Cinepolis (screen4) + Fun Cinemas (screen7) ──
        Show show4 = new Show(); // Oppenheimer @ Cinepolis Bangalore
        show4.setMovie(movie3);
        show4.setScreen(screen4);
        show4.setShowTime(LocalDateTime.now().plusHours(2));

        // ── Bangalore: new theatres ──
        Show showBlrKantara1 = new Show(); // Kantara 1PM @ Central Mall
        showBlrKantara1.setMovie(movie7);
        showBlrKantara1.setScreen(screen8);
        showBlrKantara1.setShowTime(LocalDateTime.now().withHour(13).withMinute(0).withSecond(0));

        Show showBlrKantara2 = new Show(); // Kantara 5PM @ Central Mall
        showBlrKantara2.setMovie(movie7);
        showBlrKantara2.setScreen(screen8);
        showBlrKantara2.setShowTime(LocalDateTime.now().withHour(17).withMinute(0).withSecond(0));

        Show showBlrAvengers = new Show(); // Avengers 2PM @ Lulu Mall
        showBlrAvengers.setMovie(movie1);
        showBlrAvengers.setScreen(screen9);
        showBlrAvengers.setShowTime(LocalDateTime.now().withHour(14).withMinute(0).withSecond(0));

        // ── Hyderabad: new theatres ──
        Show showHydBarbie = new Show(); // Barbie 3PM @ PVR Mall
        showHydBarbie.setMovie(movie4);
        showHydBarbie.setScreen(screen10);
        showHydBarbie.setShowTime(LocalDateTime.now().withHour(15).withMinute(0).withSecond(0));

        Show showHydRRR = new Show(); // RRR 5PM @ Inox Mall
        showHydRRR.setMovie(movie6);
        showHydRRR.setScreen(screen11);
        showHydRRR.setShowTime(LocalDateTime.now().withHour(17).withMinute(0).withSecond(0));

        // ── Mumbai: Miraj Cinemas ──
        Show showMumPushpa = new Show(); // Pushpa 4PM @ Miraj Cinemas
        showMumPushpa.setMovie(movie2);
        showMumPushpa.setScreen(screen12);
        showMumPushpa.setShowTime(LocalDateTime.now().withHour(16).withMinute(0).withSecond(0));

        Show showMumBarbie = new Show(); // Barbie 7PM @ Miraj Cinemas
        showMumBarbie.setMovie(movie4);
        showMumBarbie.setScreen(screen12);
        showMumBarbie.setShowTime(LocalDateTime.now().withHour(19).withMinute(0).withSecond(0));

        showRepository.saveAll(List.of(
            show1, show5, show7, showHyd4,
            show3, show8, showMum4,
            show4,
            showBlrKantara1, showBlrKantara2, showBlrAvengers,
            showHydBarbie, showHydRRR,
            showMumPushpa, showMumBarbie
        ));
        log.info("✓ Created 15 shows");

        createShowSeatsForShow(show1, screen1);
        createShowSeatsForShow(show5, screen2);
        createShowSeatsForShow(show7, screen5);
        createShowSeatsForShow(showHyd4, screen5);
        createShowSeatsForShow(show3, screen3);
        createShowSeatsForShow(show8, screen6);
        createShowSeatsForShow(showMum4, screen6);
        createShowSeatsForShow(show4, screen4);
        createShowSeatsForShow(showBlrKantara1, screen8);
        createShowSeatsForShow(showBlrKantara2, screen8);
        createShowSeatsForShow(showBlrAvengers, screen9);
        createShowSeatsForShow(showHydBarbie, screen10);
        createShowSeatsForShow(showHydRRR, screen11);
        createShowSeatsForShow(showMumPushpa, screen12);
        createShowSeatsForShow(showMumBarbie, screen12);
        log.info("✓ Created show seats for all shows");

        // Create Sample Users
        User user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@example.com");
        user1.setPassword("password123");
        user1.setPhoneNumber("9876543210");
        user1.setIsActive(true);

        User user2 = new User();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane@example.com");
        user2.setPassword("password456");
        user2.setPhoneNumber("9876543211");
        user2.setIsActive(true);

        userRepository.saveAll(List.of(user1, user2));
        log.info("✓ Created 2 sample users");

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("✓✓✓ Database initialization completed successfully! ✓✓✓");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void createSeatsForScreen(Screen screen, int totalSeats) {
        List<Seat> seats = new ArrayList<>();
        int seatCount = 0;

        for (int row = 1; row <= totalSeats / 10; row++) {
            for (char col = 'A'; col < 'K'; col++) {
                seatCount++;
                if (seatCount > totalSeats) break;

                Seat.SeatType seatType = Seat.SeatType.STANDARD;
                if (row <= 2) {
                    seatType = Seat.SeatType.PREMIUM;
                } else if (row >= totalSeats / 10 - 1) {
                    seatType = Seat.SeatType.RECLINER;
                }

                Seat seat = new Seat();
                seat.setSeatNumber(row + "" + col);
                seat.setSeatType(seatType);
                seat.setScreen(screen);
                seats.add(seat);
            }
            if (seatCount >= totalSeats) break;
        }

        seatRepository.saveAll(seats);
    }

    private void createShowSeatsForShow(Show show, Screen screen) {
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        List<ShowSeat> showSeats = new ArrayList<>();

        for (Seat seat : seats) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setSeat(seat);
            showSeat.setShow(show);
            showSeat.setIsBooked(false);
            showSeats.add(showSeat);
        }

        showSeatRepository.saveAll(showSeats);
    }
}
