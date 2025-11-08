package com.api.boleteria.mercadopago.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PaymentRequestDTO {

    @NotNull(message = "Debe especificar un titulo al pago.")
    private String title;           // Título del producto/entrada (Ej: Entrada de cine)

    @NotNull(message = "Debe dar una descripcion al pago.")
    private String description;     // Descripción del pago (Ej: Telefono nogro 2)

    @NotNull(message = "Debe especificar su email.")
    @Email(message = "El email debe tener un formato valido.")
    private String userEmail;       // Email del usuario que paga

    @NotNull(message = "Debe especificar la cantidad de entradas.")
    @Min(value = 1, message = "Debe comprar al menos una entrada.")
    private Integer quantity;       // Cantidad de entradas

    @NotNull(message = "Debe especificar el monto unitario.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser positivo.")
    private BigDecimal unitPrice;   // Precio unitario

    @NotNull(message = "Debe especificar el ID de la función.")
    @Positive(message = "El ID de la función debe ser un valor positivo.")
    private Long functionId;        // ID de la función asociada

    @NotNull(message = "Debe especificar las butacas.")
    @Size(min = 1, message = "Debe elegir al menos una butaca.")
    private List<String> seats;     // Butacas seleccionadas Ej: ["R1C2", "R3C5", "R3C5"]

}



