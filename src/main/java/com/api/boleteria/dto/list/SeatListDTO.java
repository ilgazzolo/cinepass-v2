package com.api.boleteria.dto.list;

public record SeatListDTO(
   Long id,
   Integer seatRowNumber,
   Integer seatColumnNumber,
   Boolean occupied
) {}
