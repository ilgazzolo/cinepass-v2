package com.api.boleteria.dto.list;

public record CardListDTO(
        Long id,
        String cardholderName,
        String expirationDate,
        String cardType
) {
}
