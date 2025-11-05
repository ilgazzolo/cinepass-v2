package com.api.boleteria.dto.detail;

import com.api.boleteria.model.enums.ScreenType;

public record CinemaDetailDTO (
        Long id,
        String name,
        ScreenType screenType,
        Boolean atmos,
        Integer rows,
        Integer columns,
        Integer seatCapacity,
        Boolean enabled
){}
