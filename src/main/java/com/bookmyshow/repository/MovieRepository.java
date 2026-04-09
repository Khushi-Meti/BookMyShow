package com.bookmyshow.repository;

import com.bookmyshow.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByName(String name);

    List<Movie> findByLanguage(String language);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.shows s JOIN s.screen sc JOIN sc.theatre t WHERE LOWER(t.location) = LOWER(:city)")
    List<Movie> findMoviesByCity(@Param("city") String city);

}
