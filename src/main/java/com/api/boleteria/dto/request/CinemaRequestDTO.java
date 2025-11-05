package com.api.boleteria.dto.request;

import com.api.boleteria.model.enums.ScreenType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CinemaRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    private String name;

    @NotNull(message = "El tipo de pantalla es obligatorio")
    private ScreenType screenType;

    @NotNull(message = "El campo atmos es obligatorio")
    private Boolean atmos;

    @NotNull(message = "La cantidad de filas es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1")
    @Max(value = 20, message = "La capacidad máxima permitida es 20 filas")
    private Integer rows;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1")
    @Max(value = 20, message = "La capacidad máxima permitida es 20 columnas")
    private Integer columns;

    @NotNull(message = "El campo habilitada es obligatorio")
    private Boolean enabled;
}
