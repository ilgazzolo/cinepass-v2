package com.api.boleteria.repository;

import com.api.boleteria.model.enums.ScreenType;
import com.api.boleteria.model.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IFunctionRepository extends JpaRepository<Function, Long> {
    boolean existsByCinemaIdAndShowtime(Long cinemaId, LocalDateTime showtime);
    List<Function> findByCinemaId(Long cinemaId);
    List<Function> findByMovieIdAndAvailableCapacityGreaterThanAndShowtimeAfterAndCinema_EnabledTrue(
            Long movieId, int capacity, LocalDateTime showtime);
    List<Function> findByCinema_ScreenTypeAndAvailableCapacityGreaterThanAndShowtimeAfterAndCinema_EnabledTrue(
            ScreenType screenType, int capacity, LocalDateTime showtime);
    List<Function> findByCinemaIdAndAvailableCapacityGreaterThanAndShowtimeAfter(
            Long cinemaId, int availableCapacity, LocalDateTime showtime);



}
