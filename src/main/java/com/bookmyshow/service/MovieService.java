package com.bookmyshow.service;

import com.bookmyshow.dto.MovieDTO;
import com.bookmyshow.model.Movie;
import com.bookmyshow.repository.MovieRepository;
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
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieDTO createMovie(MovieDTO movieDTO) {
        log.info("Creating movie: {}", movieDTO.getName());
        Movie movie = convertDTOToEntity(movieDTO);
        Movie savedMovie = movieRepository.save(movie);
        return convertEntityToDTO(savedMovie);
    }

    @Transactional(readOnly = true)
    public MovieDTO getMovieById(Long id) {
        log.info("Fetching movie with id: {}", id);
        Optional<Movie> movie = movieRepository.findById(id);
        return movie.map(this::convertEntityToDTO).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getAllMovies() {
        log.info("Fetching all movies");
        return movieRepository.findAll().stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByLanguage(String language) {
        log.info("Fetching movies by language: {}", language);
        return movieRepository.findByLanguage(language).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByCity(String city) {
        log.info("Fetching movies by city: {}", city);
        return movieRepository.findMoviesByCity(city).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
        log.info("Updating movie with id: {}", id);
        Optional<Movie> optionalMovie = movieRepository.findById(id);
        if (optionalMovie.isPresent()) {
            Movie movie = optionalMovie.get();
            movie.setName(movieDTO.getName());
            movie.setDuration(movieDTO.getDuration());
            movie.setLanguage(movieDTO.getLanguage());
            Movie updatedMovie = movieRepository.save(movie);
            return convertEntityToDTO(updatedMovie);
        }
        return null;
    }

    public void deleteMovie(Long id) {
        log.info("Deleting movie with id: {}", id);
        movieRepository.deleteById(id);
    }

    private MovieDTO convertEntityToDTO(Movie movie) {
        return new MovieDTO(
                movie.getId(),
                movie.getName(),
                movie.getDuration(),
                movie.getLanguage()
        );
    }

    private Movie convertDTOToEntity(MovieDTO movieDTO) {
        Movie movie = new Movie();
        movie.setName(movieDTO.getName());
        movie.setDuration(movieDTO.getDuration());
        movie.setLanguage(movieDTO.getLanguage());
        return movie;
    }

}
