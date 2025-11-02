package com.api.boleteria.dto.detail;

public record FunctionDetailDTO(
        Long id,
        String date,
        Long cinemaId,
        String cinemaName,
        Long movieId,
        String movieName,
        Integer availableCapacity,
        Integer runTime

) {}
