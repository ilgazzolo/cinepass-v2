package com.api.boleteria.mercadopago.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequestDTO {
    private String title;          // Título del producto/entrada
    private String description;    // Descripción del pago
    private String userEmail;      // Email del usuario que paga
    private Integer quantity;      // Cantidad de entradas
    private BigDecimal unitPrice;  // Precio unitario
    private Long ticketId;         // ID del ticket asociado
}

