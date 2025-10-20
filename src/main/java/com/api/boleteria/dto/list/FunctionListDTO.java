package com.api.boleteria.dto.list;

import java.time.LocalDate;
import java.time.LocalTime;

public record FunctionListDTO(
        Long id,
        LocalDate date,
        LocalTime time,
        Long cinemaId,
        Long movieId
) {}
