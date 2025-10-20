package com.api.boleteria.validators;

import com.api.boleteria.dto.request.MovieRequestDTO;
import com.api.boleteria.exception.BadRequestException;

/**
 * Clase de validación para los datos del MovieRequestDTO.
 * Contiene métodos estáticos que verifican que los campos obligatorios estén presentes
 * y que cumplan con las reglas de negocio, como longitud mínima/máxima o rangos numéricos.
 */
public class MovieValidator {

    private static final int TITLE_MIN = 2;
    private static final int TITLE_MAX = 100;
    private static final int GENRE_MIN = 3;
    private static final int GENRE_MAX = 50;
    private static final int DIRECTOR_MIN = 3;
    private static final int DIRECTOR_MAX = 50;
    private static final int SYNOPSIS_MIN = 10;
    private static final int SYNOPSIS_MAX = 500;
    private static final int MAX_DURATION = 400;

    /**
     * Valida todos los campos obligatorios y reglas de formato para un MovieRequestDTO.
     *
     * @param dto DTO con los datos de la película a validar.
     * @throws BadRequestException si algún campo no cumple las reglas definidas.
     */
    public static void validateFields(MovieRequestDTO dto) {
        validateTitle(dto.getTitle());
        validateDuration(dto.getDuration());
        validateGenre(dto.getGenre());
        validateDirector(dto.getDirector());
        validateClassification(dto.getClassification());
        validateSynopsis(dto.getSynopsis());
    }

    /**
     * Valida el título de la película.
     *
     * @param title Título a validar.
     * @throws BadRequestException si es nulo, vacío o no cumple con la longitud permitida.
     */
    public static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("El título es obligatorio.");
        }
        if (title.length() < TITLE_MIN || title.length() > TITLE_MAX) {
            throw new BadRequestException("El título debe tener entre " + TITLE_MIN + " y " + TITLE_MAX + " caracteres.");
        }
    }

    /**
     * Valida la duración de la película.
     *
     * @param duration Duración en minutos.
     * @throws BadRequestException si es nula, no positiva o excede el máximo permitido.
     */
    public static void validateDuration(Integer duration) {
        if (duration == null) {
            throw new BadRequestException("La duración es obligatoria.");
        }
        if (duration <= 0) {
            throw new BadRequestException("La duración debe ser un número positivo.");
        }
        if (duration > MAX_DURATION) {
            throw new BadRequestException("La duración no puede superar los " + MAX_DURATION + " minutos.");
        }
    }

    /**
     * Valida el género de la película.
     *
     * @param genre Género a validar.
     * @throws BadRequestException si es nulo, vacío o no cumple con la longitud permitida.
     */
    public static void validateGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            throw new BadRequestException("El género es obligatorio.");
        }
        if (genre.length() < GENRE_MIN || genre.length() > GENRE_MAX) {
            throw new BadRequestException("El género debe tener entre " + GENRE_MIN + " y " + GENRE_MAX + " caracteres.");
        }
    }


    /**
     * Valida el director de la película.
     *
     * @param director Nombre del director.
     * @throws BadRequestException si es nulo, vacío o no cumple con la longitud permitida.
     */
    public static void validateDirector(String director) {
        if (director == null || director.isBlank()) {
            throw new BadRequestException("El director es obligatorio.");
        }
        if (director.length() < DIRECTOR_MIN || director.length() > DIRECTOR_MAX) {
            throw new BadRequestException("El director debe tener entre " + DIRECTOR_MIN + " y " + DIRECTOR_MAX + " caracteres.");
        }
    }

    /**
     * Valida la clasificación de la película.
     *
     * @param classification Clasificación a validar.
     * @throws BadRequestException si es nula o vacía.
     */
    public static void validateClassification(String classification) {
        if (classification == null || classification.isBlank()) {
            throw new BadRequestException("La clasificación es obligatoria.");
        }
    }

    /**
     * Valida la sinopsis de la película.
     *
     * @param synopsis Sinopsis a validar.
     * @throws BadRequestException si es nula, vacía o no cumple con la longitud permitida.
     */
    public static void validateSynopsis(String synopsis) {
        if (synopsis == null || synopsis.isBlank()) {
            throw new BadRequestException("La sinopsis es obligatoria.");
        }
        if (synopsis.length() < SYNOPSIS_MIN || synopsis.length() > SYNOPSIS_MAX) {
            throw new BadRequestException("La sinopsis debe tener entre " + SYNOPSIS_MIN + " y " + SYNOPSIS_MAX + " caracteres.");
        }
    }

    /**
     * Valida que el ID de la película sea un número positivo y no nulo.
     *
     * @param id ID de la película a validar.
     * @throws IllegalArgumentException si el ID es nulo o menor o igual a cero.
     */
    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la película debe ser un número positivo.");
        }
    }
}
