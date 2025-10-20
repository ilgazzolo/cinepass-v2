package com.api.boleteria.validators;

import com.api.boleteria.dto.request.FunctionRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.model.Cinema;
import com.api.boleteria.model.Function;
import com.api.boleteria.model.Movie;

import java.time.LocalDateTime;
import java.util.List;

public class FunctionValidator {

    /**
     * Valida todos los campos del DTO FunctionRequestDTO.
     *
     * @param dto DTO con los datos de la función a validar.
     * @throws BadRequestException si algún campo no cumple las reglas de validación.
     */
    public static void validateFields(FunctionRequestDTO dto) {
        validateShowtime(dto.getShowtime());
        validateCinemaId(dto.getCinemaId());
        validateMovieId(dto.getMovieId());
    }

    /**
     * Valida la fecha y hora de la función.
     *
     * @param showtime Fecha y hora de la función.
     * @throws BadRequestException si la fecha es nula o está en el pasado.
     */
    public static void validateShowtime(LocalDateTime showtime) {
        if (showtime == null) {
            throw new BadRequestException("La fecha de la función no puede ser nula.");
        }
        if (showtime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de la función no puede estar en el pasado.");
        }
    }

    /**
     * Valida el ID del cine.
     *
     * @param cinemaId ID del cine.
     * @throws BadRequestException si el ID es nulo o menor o igual a cero.
     */
    public static void validateCinemaId(Long cinemaId) {
        if (cinemaId == null || cinemaId <= 0) {
            throw new BadRequestException("El ID del cine no es válido.");
        }
    }

    /**
     * Valida el ID de la película.
     *
     * @param movieId ID de la película.
     * @throws BadRequestException si el ID es nulo o menor o igual a cero.
     */
    public static void validateMovieId(Long movieId) {
        if (movieId == null || movieId <= 0) {
            throw new BadRequestException("El ID de la película no es válido.");
        }
    }

    /**
     * Valida que no haya solapamiento de horarios con funciones ya existentes en la misma sala.
     *
     * @param dto DTO con la nueva función a validar.
     * @param movie Película que se va a proyectar.
     * @param functionsInTheCinema Lista de funciones existentes en esa sala.
     * @throws BadRequestException si hay superposición de horarios.
     */
    public static void validateSchedule(FunctionRequestDTO dto, Movie movie, List<Function> functionsInTheCinema) {
        LocalDateTime newStart = dto.getShowtime();
        LocalDateTime newEnd = newStart.plusMinutes(movie.getDuration());

        for (Function f : functionsInTheCinema) {
            LocalDateTime existingStart = f.getShowtime();
            LocalDateTime existingEnd = existingStart.plusMinutes(f.getMovie().getDuration());

            boolean overlap = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
            if (overlap) {
                throw new BadRequestException("Ya existe una función en esa sala que se solapa con el horario.");
            }
        }
    }

    /**
     * Valida que la fecha de la función no sea mayor a dos años desde la fecha actual.
     *
     * @param dto DTO de la función a validar.
     * @throws BadRequestException si la fecha excede el límite de dos años.
     */
    public static void validateMaxTwoYears(FunctionRequestDTO dto) {
        if (dto.getShowtime().isAfter(LocalDateTime.now().plusYears(2))) {
            throw new BadRequestException("La fecha de la función es demasiado lejana.");
        }
    }

    /**
     * Valida que la sala esté habilitada para proyectar funciones.
     *
     * @param cinema Sala donde se proyectará la función.
     * @throws BadRequestException si la sala no está habilitada.
     */
    public static void validateEnabledCinema(Cinema cinema) {
        if (!cinema.getEnabled()) {
            throw new BadRequestException("La sala " + cinema.getName() + " no está habilitada.");
        }
    }

    /**
     * Valida que el ID de la función sea válido (no nulo y mayor a cero).
     *
     * @param id ID de la función.
     * @throws BadRequestException si el ID es inválido.
     */
    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID de la función debe ser un número positivo.");
        }
    }
}
