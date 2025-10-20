package com.api.boleteria.validators;

import com.api.boleteria.dto.request.CinemaRequestDTO;
import com.api.boleteria.exception.BadRequestException;

/**
 * Clase encargada de validar los datos de entrada para la entidad Cinema.
 *
 * Realiza validaciones sobre los campos obligatorios y rangos permitidos
 * para los atributos de un CinemaRequestDTO.
 */
public class CinemaValidator {

    /**
     * Valida todos los campos del DTO de solicitud de Cinema.
     *
     * @param dto DTO con los datos del cine a validar.
     * @throws BadRequestException si alguna validación falla.
     */
    public static void validateFields(CinemaRequestDTO dto) {
        validateName(dto.getName());
        validateScreenType(dto.getScreenType());
        validateCapacity(dto.getCapacity());
        validateAtmos(dto.getAtmos());
        validateEnabled(dto.getEnabled());
    }

    /**
     * Valida el nombre del cine.
     *
     * @param name Nombre a validar.
     * @throws BadRequestException si el nombre es nulo, vacío o excede 100 caracteres.
     */
    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("El nombre no puede ser nulo ni estar vacío.");
        }
        if (name.length() > 100) {
            throw new BadRequestException("El nombre debe tener máximo 100 caracteres.");
        }
    }

    /**
     * Valida el tipo de pantalla.
     *
     * @param screenType Tipo de pantalla a validar.
     * @throws BadRequestException si es nulo.
     */
    public static void validateScreenType(Object screenType) {
        if (screenType == null) {
            throw new BadRequestException("El tipo de pantalla no puede ser nulo.");
        }
    }

    /**
     * Valida la capacidad de la sala.
     *
     * @param capacity Capacidad a validar.
     * @throws BadRequestException si es nula o no está entre 1 y 200.
     */
    public static void validateCapacity(Integer capacity) {
        if (capacity == null) {
            throw new BadRequestException("La capacidad no puede ser nula.");
        }
        if (capacity < 1 || capacity > 200) {
            throw new BadRequestException("La capacidad debe ser entre 1 y 200.");
        }
    }

    /**
     * Valida el atributo Atmos.
     *
     * @param atmos Valor a validar.
     * @throws BadRequestException si es nulo.
     */
    public static void validateAtmos(Boolean atmos) {
        if (atmos == null) {
            throw new BadRequestException("El atributo Atmos no puede ser nulo.");
        }
    }

    /**
     * Valida si la sala está habilitada.
     *
     * @param enabled Valor a validar.
     * @throws BadRequestException si es nulo.
     */
    public static void validateEnabled(Boolean enabled) {
        if (enabled == null) {
            throw new BadRequestException("El atributo 'habilitada' no puede ser nulo.");
        }
    }

    /**
     * Valida que el ID de la sala sea válido (no nulo y mayor a 0).
     *
     * @param id ID de la sala a validar.
     * @throws IllegalArgumentException si el ID es nulo o menor o igual a cero.
     */
    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID de la sala debe ser mayor a 0 y no puede ser nulo.");
        }
    }
}
