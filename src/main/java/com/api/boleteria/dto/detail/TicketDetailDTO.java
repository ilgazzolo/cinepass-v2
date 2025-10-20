package com.api.boleteria.dto.detail;

public record TicketDetailDTO(
        Long id,
        String purchaseDate,
        String movieTittle,
        Long cinemaId,
        String purchaseTime,
        Double ticketPrice

) {}
