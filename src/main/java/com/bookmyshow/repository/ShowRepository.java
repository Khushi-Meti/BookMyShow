package com.bookmyshow.repository;

import com.bookmyshow.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theatre WHERE s.movie.id = :movieId")
    List<Show> findByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theatre WHERE sc.id = :screenId")
    List<Show> findByScreenId(@Param("screenId") Long screenId);

    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theatre WHERE s.id = :id")
    Optional<Show> findByIdWithDetails(@Param("id") Long id);

    List<Show> findByShowTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Show> findByMovieIdAndScreenId(Long movieId, Long screenId);

}
