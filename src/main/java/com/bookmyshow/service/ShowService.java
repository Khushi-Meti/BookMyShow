package com.bookmyshow.service;

import com.bookmyshow.dto.ScreenDTO;
import com.bookmyshow.dto.ShowDTO;
import com.bookmyshow.dto.TheatreDTO;
import com.bookmyshow.model.Movie;
import com.bookmyshow.model.Screen;
import com.bookmyshow.model.Show;
import com.bookmyshow.repository.MovieRepository;
import com.bookmyshow.repository.ScreenRepository;
import com.bookmyshow.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public ShowDTO createShow(ShowDTO showDTO) {
        log.info("Creating show for movie id: {}, screen id: {}", 
                 showDTO.getMovieId(), showDTO.getScreenId());
        
        Optional<Movie> movie = movieRepository.findById(showDTO.getMovieId());
        Optional<Screen> screen = screenRepository.findById(showDTO.getScreenId());
        
        if (movie.isPresent() && screen.isPresent()) {
            Show show = convertDTOToEntity(showDTO);
            show.setMovie(movie.get());
            show.setScreen(screen.get());
            Show savedShow = showRepository.save(show);
            return convertEntityToDTO(savedShow);
        }
        
        log.warn("Movie or Screen not found. Movie ID: {}, Screen ID: {}", 
                 showDTO.getMovieId(), showDTO.getScreenId());
        return null;
    }

    @Transactional(readOnly = true)
    public ShowDTO getShowById(Long id) {
        log.info("Fetching show with id: {}", id);
        Optional<Show> show = showRepository.findByIdWithDetails(id);
        return show.map(this::convertEntityToDTO).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ShowDTO> getAllShows() {
        log.info("Fetching all shows");
        return showRepository.findAll().stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowDTO> getShowsByMovieId(Long movieId) {
        log.info("Fetching all shows for movie id: {}", movieId);
        return showRepository.findByMovieId(movieId).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowDTO> getShowsByScreenId(Long screenId) {
        log.info("Fetching all shows for screen id: {}", screenId);
        return showRepository.findByScreenId(screenId).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    public ShowDTO updateShow(Long id, ShowDTO showDTO) {
        log.info("Updating show with id: {}", id);
        Optional<Show> optionalShow = showRepository.findById(id);
        
        if (optionalShow.isPresent()) {
            Show show = optionalShow.get();
            show.setShowTime(showDTO.getShowTime());
            
            if (showDTO.getMovieId() != null) {
                Optional<Movie> movie = movieRepository.findById(showDTO.getMovieId());
                movie.ifPresent(show::setMovie);
            }
            
            if (showDTO.getScreenId() != null) {
                Optional<Screen> screen = screenRepository.findById(showDTO.getScreenId());
                screen.ifPresent(show::setScreen);
            }
            
            Show updatedShow = showRepository.save(show);
            return convertEntityToDTO(updatedShow);
        }
        return null;
    }

    public void deleteShow(Long id) {
        log.info("Deleting show with id: {}", id);
        showRepository.deleteById(id);
    }

    private ShowDTO convertEntityToDTO(Show show) {
        TheatreDTO theatreDTO = new TheatreDTO(
                show.getScreen().getTheatre().getId(),
                show.getScreen().getTheatre().getName(),
                show.getScreen().getTheatre().getLocation()
        );
        ScreenDTO screenDTO = new ScreenDTO(
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getScreen().getTotalSeats(),
                theatreDTO
        );
        return new ShowDTO(
                show.getId(),
                show.getShowTime(),
                show.getMovie().getId(),
                show.getScreen().getId(),
                screenDTO
        );
    }

    private Show convertDTOToEntity(ShowDTO showDTO) {
        Show show = new Show();
        show.setShowTime(showDTO.getShowTime());
        return show;
    }

}
