package com.api.boleteria.repository;

import com.api.boleteria.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMovieRepository extends JpaRepository<Movie,Long> {
    boolean existsByTitle(String title);
    List<Movie> findByMovieGenre(String genre);
    boolean existsByTitleAndIdNot(String title, Long id);
}
