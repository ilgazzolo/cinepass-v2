package com.api.boleteria.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketRequestDTO {

        @NotNull(message = "Debe especificar el ID de la función.")
        @Positive(message = "El ID de la función debe ser un valor positivo.")
        private Long functionId;

        @NotNull(message = "Debe especificar la cantidad de tickets.")
        @Min(value = 1, message = "Debe comprar al menos un ticket.")
        private Integer quantity;

        //agregar precio final(amout), id de peli, array string(butacas)
}
