package com.bookmyshow.service;

import com.bookmyshow.dto.TheatreDTO;
import com.bookmyshow.model.Theatre;
import com.bookmyshow.repository.TheatreRepository;
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
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreDTO createTheatre(TheatreDTO theatreDTO) {
        log.info("Creating theatre: {}", theatreDTO.getName());
        Theatre theatre = convertDTOToEntity(theatreDTO);
        Theatre savedTheatre = theatreRepository.save(theatre);
        return convertEntityToDTO(savedTheatre);
    }

    @Transactional(readOnly = true)
    public TheatreDTO getTheatreById(Long id) {
        log.info("Fetching theatre with id: {}", id);
        Optional<Theatre> theatre = theatreRepository.findById(id);
        return theatre.map(this::convertEntityToDTO).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TheatreDTO> getAllTheatres() {
        log.info("Fetching all theatres");
        return theatreRepository.findAll().stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getCities() {
        log.info("Fetching all cities");
        return theatreRepository.findDistinctLocations();
    }

    @Transactional(readOnly = true)
    public List<TheatreDTO> getTheatresByLocation(String location) {
        log.info("Fetching theatres by location: {}", location);
        return theatreRepository.findByLocation(location).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TheatreDTO> getTheatresByCityAndMovie(String city, Long movieId) {
        log.info("Fetching theatres by city: {} and movie: {}", city, movieId);
        return theatreRepository.findTheatresByCityAndMovie(city, movieId).stream()
                .map(this::convertEntityToDTO)
                .collect(Collectors.toList());
    }

    public TheatreDTO updateTheatre(Long id, TheatreDTO theatreDTO) {
        log.info("Updating theatre with id: {}", id);
        Optional<Theatre> optionalTheatre = theatreRepository.findById(id);
        if (optionalTheatre.isPresent()) {
            Theatre theatre = optionalTheatre.get();
            theatre.setName(theatreDTO.getName());
            theatre.setLocation(theatreDTO.getLocation());
            Theatre updatedTheatre = theatreRepository.save(theatre);
            return convertEntityToDTO(updatedTheatre);
        }
        return null;
    }

    public void deleteTheatre(Long id) {
        log.info("Deleting theatre with id: {}", id);
        theatreRepository.deleteById(id);
    }

    private TheatreDTO convertEntityToDTO(Theatre theatre) {
        return new TheatreDTO(
                theatre.getId(),
                theatre.getName(),
                theatre.getLocation()
        );
    }

    private Theatre convertDTOToEntity(TheatreDTO theatreDTO) {
        Theatre theatre = new Theatre();
        theatre.setName(theatreDTO.getName());
        theatre.setLocation(theatreDTO.getLocation());
        return theatre;
    }

}
