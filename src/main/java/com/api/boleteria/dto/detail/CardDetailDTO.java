package com.api.boleteria.dto.detail;

public record CardDetailDTO(
        Long id,
        String cardNumber,
        String cardholderName,
        String expirationDate,
        String issueDate,
        String cardType,
        Double balance,
        Long userId
) {
}
