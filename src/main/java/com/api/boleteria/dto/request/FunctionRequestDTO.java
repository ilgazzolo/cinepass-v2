package com.api.boleteria.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FunctionRequestDTO {
    @NotNull(message = "La fecha de la funcion es obligatoria")
    @Future(message = "la funcion no puede tener una fecha anterior a la actual")
    private LocalDateTime showtime;

    @NotNull(message = "La sala es obligatoria")
    private Long cinemaId;

    @NotNull(message = "La pelicula es obligatoria")
    @Positive(message = "La id de la pelicula debe ser positiva")
    private Long movieId;
}
