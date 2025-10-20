package com.api.boleteria.mercadopago.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDTO {
    private String preferenceId;
    private String initPoint;

    public PaymentResponseDTO(String preferenceId, String initPoint) {
        this.preferenceId = preferenceId;
        this.initPoint = initPoint;
    }

}

