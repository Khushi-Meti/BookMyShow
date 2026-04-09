package com.bookmyshow.controller;

import com.bookmyshow.dto.ShowDTO;
import com.bookmyshow.service.ShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ShowController {

    private final ShowService showService;

    /**
     * Create a new show
     * POST /shows
     */
    @PostMapping
    public ResponseEntity<ShowDTO> createShow(@RequestBody ShowDTO showDTO) {
        log.info("POST request to create show for movie id: {}, screen id: {}", 
                 showDTO.getMovieId(), showDTO.getScreenId());
        ShowDTO createdShow = showService.createShow(showDTO);
        return new ResponseEntity<>(createdShow, HttpStatus.CREATED);
    }

    /**
     * Get show by ID
     * GET /shows/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShowDTO> getShowById(@PathVariable Long id) {
        log.info("GET request to fetch show with id: {}", id);
        ShowDTO show = showService.getShowById(id);
        if (show != null) {
            return new ResponseEntity<>(show, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Get all shows
     * GET /shows
     */
    @GetMapping
    public ResponseEntity<List<ShowDTO>> getAllShows() {
        log.info("GET request to fetch all shows");
        List<ShowDTO> shows = showService.getAllShows();
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

    /**
     * Get all shows for a specific movie
     * GET /shows/movie/{movieId}
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowDTO>> getShowsByMovieId(@PathVariable Long movieId) {
        log.info("GET request to fetch all shows for movie id: {}", movieId);
        List<ShowDTO> shows = showService.getShowsByMovieId(movieId);
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

    /**
     * Get all shows for a specific screen
     * GET /shows/screen/{screenId}
     */
    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<ShowDTO>> getShowsByScreenId(@PathVariable Long screenId) {
        log.info("GET request to fetch all shows for screen id: {}", screenId);
        List<ShowDTO> shows = showService.getShowsByScreenId(screenId);
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

    /**
     * Update show
     * PUT /shows/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShowDTO> updateShow(@PathVariable Long id, @RequestBody ShowDTO showDTO) {
        log.info("PUT request to update show with id: {}", id);
        ShowDTO updatedShow = showService.updateShow(id, showDTO);
        if (updatedShow != null) {
            return new ResponseEntity<>(updatedShow, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Delete show
     * DELETE /shows/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        log.info("DELETE request to delete show with id: {}", id);
        showService.deleteShow(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
