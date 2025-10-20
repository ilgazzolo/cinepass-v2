package com.api.boleteria.dto.list;

import java.time.LocalDateTime;

public record TicketListDTO(
        Long id,
        Long funcionId,
        String movieTittle,
        LocalDateTime date,
        Double ticketPrice
) {}
