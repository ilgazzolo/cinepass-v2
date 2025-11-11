package com.api.boleteria.dto.detail;

import java.util.List;

public record MovieDetailDTO(
        Long id,
        String title,
        String originalLanguage,
        String releaseDate,
        Integer runtime,
        List<String> genres,
        String overview,
        String imdbId,
        Double voteAverage,
        Integer voteCount,
        String posterUrl,
        String bannerUrl,
        Boolean adult
){}
