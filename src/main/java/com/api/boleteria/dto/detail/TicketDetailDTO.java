package com.api.boleteria.dto.detail;

public record TicketDetailDTO(
        Long id,
        String purchaseDate,
        String movieTitle,
        Long cinemaId,
        String purchaseTime,
        Double ticketPrice
        // array de butaca
) {}
