package com.api.boleteria.dto.detail;

import java.util.List;

public record MovieDetailDTO(
        Long id,
        String title,
        String originalLanguage,
        String releaseDate,
        int runtime,
        List<String> genres,
        String overview,
        String imdbId,
        double voteAverage,
        int voteCount,
        String posterUrl,
        String bannerUrl
){}
