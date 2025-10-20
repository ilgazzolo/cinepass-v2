package com.api.boleteria.dto.list;

public record CinemaListDTO (
    Long id,
    String name,
    Integer seatCapacity,
    Boolean enabled
){}
