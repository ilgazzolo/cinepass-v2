package com.api.boleteria.dto.list;

public record MovieListDTO(
        Long id,
        String title,
        String posterUrl,
        double voteAverage,
        String releaseDate
) {}
