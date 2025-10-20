package com.api.boleteria.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class MovieRequestDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 100, message = "El título debe tener entre 2 y 100 caracteres")
    private String title;

    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser un número positivo")
    @Max(value = 400, message = "La duración no puede superar los 400 minutos")
    private Integer duration;

    @NotBlank(message = "El género es obligatorio")
    @Size(min = 3, max = 50, message = "El género debe tener entre 3 y 50 caracteres")
    private String genre;

    @NotBlank(message = "El director es obligatorio")
    @Size(min = 3, max = 50, message = "El director debe tener entre 3 y 50 caracteres")
    private String director;

    @NotBlank(message = "La clasificación es obligatoria")
    private String classification;

    @NotBlank(message = "La sinopsis es obligatoria")
    @Size(min = 10, max = 500, message = "La sinopsis debe tener entre 10 y 500 caracteres")
    private String synopsis;
}
