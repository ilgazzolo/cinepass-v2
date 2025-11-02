package com.api.boleteria.dto.list;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketListDTO(
        Long id,
        Long functionId,
        String movieTitle,
        LocalDateTime purchaseDateTime,
        BigDecimal ticketPrice,
        Integer quantity
) {}
