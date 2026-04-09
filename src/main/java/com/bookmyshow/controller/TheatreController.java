package com.bookmyshow.controller;

import com.bookmyshow.dto.TheatreDTO;
import com.bookmyshow.service.TheatreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TheatreController {

    private final TheatreService theatreService;

    /**
     * Create a new theatre
     * POST /theatres
     */
    @PostMapping
    public ResponseEntity<TheatreDTO> createTheatre(@RequestBody TheatreDTO theatreDTO) {
        log.info("POST request to create theatre: {}", theatreDTO.getName());
        TheatreDTO createdTheatre = theatreService.createTheatre(theatreDTO);
        return new ResponseEntity<>(createdTheatre, HttpStatus.CREATED);
    }

    /**
     * Get theatre by ID
     * GET /theatres/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TheatreDTO> getTheatreById(@PathVariable Long id) {
        log.info("GET request to fetch theatre with id: {}", id);
        TheatreDTO theatre = theatreService.getTheatreById(id);
        if (theatre != null) {
            return new ResponseEntity<>(theatre, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Get all theatres
     * GET /theatres
     */
    @GetMapping
    public ResponseEntity<List<TheatreDTO>> getAllTheatres() {
        log.info("GET request to fetch all theatres");
        List<TheatreDTO> theatres = theatreService.getAllTheatres();
        return new ResponseEntity<>(theatres, HttpStatus.OK);
    }

    /**
     * Get all cities
     * GET /theatres/cities
     */
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        log.info("GET request to fetch all cities");
        List<String> cities = theatreService.getCities();
        return new ResponseEntity<>(cities, HttpStatus.OK);
    }

    /**
     * Get theatres by location
     * GET /theatres/location/{location}
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<List<TheatreDTO>> getTheatresByLocation(@PathVariable String location) {
        log.info("GET request to fetch theatres by location: {}", location);
        List<TheatreDTO> theatres = theatreService.getTheatresByLocation(location);
        return new ResponseEntity<>(theatres, HttpStatus.OK);
    }

    /**
     * Get theatres by city and movie
     * GET /theatres/city/{city}/movie/{movieId}
     */
    @GetMapping("/city/{city}/movie/{movieId}")
    public ResponseEntity<List<TheatreDTO>> getTheatresByCityAndMovie(@PathVariable String city, @PathVariable Long movieId) {
        log.info("GET request to fetch theatres by city: {} and movie: {}", city, movieId);
        List<TheatreDTO> theatres = theatreService.getTheatresByCityAndMovie(city, movieId);
        return new ResponseEntity<>(theatres, HttpStatus.OK);
    }

    /**
     * Update theatre
     * PUT /theatres/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TheatreDTO> updateTheatre(@PathVariable Long id, @RequestBody TheatreDTO theatreDTO) {
        log.info("PUT request to update theatre with id: {}", id);
        TheatreDTO updatedTheatre = theatreService.updateTheatre(id, theatreDTO);
        if (updatedTheatre != null) {
            return new ResponseEntity<>(updatedTheatre, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Delete theatre
     * DELETE /theatres/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheatre(@PathVariable Long id) {
        log.info("DELETE request to delete theatre with id: {}", id);
        theatreService.deleteTheatre(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
