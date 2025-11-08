package com.api.boleteria.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TicketRequestDTO {

        @NotNull(message = "Debe especificar el ID de la función.")
        @Positive(message = "El ID de la función debe ser un valor positivo.")
        private Long functionId;

        @NotNull(message = "Debe especificar el monto unitario por entrada.")
        @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
        private BigDecimal unitPrice;

        @NotNull(message = "Debe especificar la cantidad de entradas.")
        @Min(value = 1, message = "Debe comprar al menos una entrada.")
        private Integer quantity;

        @NotNull(message = "Debe especificar el monto total.")
        @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
        private BigDecimal totalAmount;

        @NotNull(message = "Debe especificar las butacas.")
        @Min(value = 1, message = "Debe elegir al menos una butaca.")
        private List<String> seats;  // Butacas seleccionadas Ej: ["R1C2", "R3C5", "R3C5"]
}