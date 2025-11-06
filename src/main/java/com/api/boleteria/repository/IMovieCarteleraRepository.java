package com.api.boleteria.repository;

import com.api.boleteria.model.MovieCartelera;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IMovieCarteleraRepository extends JpaRepository<MovieCartelera, Long> {
}
