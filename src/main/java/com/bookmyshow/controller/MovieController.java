package com.bookmyshow.controller;

import com.bookmyshow.dto.MovieDTO;
import com.bookmyshow.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MovieController {

    private final MovieService movieService;

    /**
     * Create a new movie
     * POST /movies
     */
    @PostMapping
    public ResponseEntity<MovieDTO> createMovie(@RequestBody MovieDTO movieDTO) {
        log.info("POST request to create movie: {}", movieDTO.getName());
        MovieDTO createdMovie = movieService.createMovie(movieDTO);
        return new ResponseEntity<>(createdMovie, HttpStatus.CREATED);
    }

    /**
     * Get movie by ID
     * GET /movies/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        log.info("GET request to fetch movie with id: {}", id);
        MovieDTO movie = movieService.getMovieById(id);
        if (movie != null) {
            return new ResponseEntity<>(movie, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Get all movies
     * GET /movies
     */
    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        log.info("GET request to fetch all movies");
        List<MovieDTO> movies = movieService.getAllMovies();
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    /**
     * Get movies by language
     * GET /movies/language/{language}
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<List<MovieDTO>> getMoviesByLanguage(@PathVariable String language) {
        log.info("GET request to fetch movies by language: {}", language);
        List<MovieDTO> movies = movieService.getMoviesByLanguage(language);
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    /**
     * Get movies by city
     * GET /movies/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<MovieDTO>> getMoviesByCity(@PathVariable String city) {
        log.info("GET request to fetch movies by city: {}", city);
        List<MovieDTO> movies = movieService.getMoviesByCity(city);
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    /**
     * Update movie
     * PUT /movies/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Long id, @RequestBody MovieDTO movieDTO) {
        log.info("PUT request to update movie with id: {}", id);
        MovieDTO updatedMovie = movieService.updateMovie(id, movieDTO);
        if (updatedMovie != null) {
            return new ResponseEntity<>(updatedMovie, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Delete movie
     * DELETE /movies/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        log.info("DELETE request to delete movie with id: {}", id);
        movieService.deleteMovie(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
