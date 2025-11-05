package com.api.boleteria.dto.list;

public record SeatListDTO(
   Long id,
   Integer rowNumber,
   Integer columnNumber,
   Boolean occupied
) {}
