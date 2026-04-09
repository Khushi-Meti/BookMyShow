package com.bookmyshow.repository;

import com.bookmyshow.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    Optional<Theatre> findByName(String name);

    List<Theatre> findByLocation(String location);

    @Query("SELECT DISTINCT t.location FROM Theatre t")
    List<String> findDistinctLocations();

    @Query("SELECT DISTINCT t FROM Theatre t JOIN t.screens sc JOIN sc.shows s WHERE t.location = :city AND s.movie.id = :movieId")
    List<Theatre> findTheatresByCityAndMovie(@Param("city") String city, @Param("movieId") Long movieId);

}
