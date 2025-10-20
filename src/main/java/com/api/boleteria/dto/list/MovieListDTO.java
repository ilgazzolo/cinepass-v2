package com.api.boleteria.dto.list;

public record MovieListDTO(
        Long id,
        String title,
        Integer duration,
        String genre,
        String director
) {}
