package com.api.boleteria.dto.detail;

import java.util.List;

public record MovieDetailDTO(
        Long id,
        String title,
        Integer duration,
        String genre,
        String director,
        String rating,
        String synopsis
){}
