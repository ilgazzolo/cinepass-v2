package com.api.boleteria.dto.detail;

import java.math.BigDecimal;
import java.util.List;

public record TicketDetailDTO(
        Long id,
        String movieTitle,
        Long cinemaId,
        String purchaseDate,
        String purchaseTime,
        BigDecimal ticketPrice,
        Integer quantity,
        List<String> seats
) {}

