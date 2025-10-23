package com.api.boleteria.mercadopago.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequestDTO {
    private String title;
    private String description;
    private String userEmail;
    private Integer quantity;
    private BigDecimal unitPrice;
}
