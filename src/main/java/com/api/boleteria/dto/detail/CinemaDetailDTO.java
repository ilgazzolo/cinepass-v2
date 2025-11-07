package com.api.boleteria.dto.detail;

import com.api.boleteria.model.enums.ScreenType;

public record CinemaDetailDTO (
        Long id,
        String name,
        ScreenType screenType,
        Boolean atmos,
        Integer rowSeat,
        Integer columnSeat,
        Integer seatCapacity,
        Boolean enabled
){}
