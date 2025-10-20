package com.api.boleteria.validators;

import com.api.boleteria.dto.request.TicketRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.model.Card;
import com.api.boleteria.model.Function;

import static com.api.boleteria.service.TicketService.TICKET_PRICE;

/**
 * Clase de validación para operaciones relacionadas con tickets.
 * Contiene métodos estáticos que validan los datos de entrada para la compra de tickets,
 * la capacidad disponible en la función y el saldo disponible en la tarjeta del usuario.
 */
public class TicketValidator {

    /**
     * Valida los campos básicos de un TicketRequestDTO, como el ID de la función y la cantidad de tickets.
     *
     * @param dto DTO con los datos de la solicitud de compra de tickets.
     * @throws BadRequestException si el ID de la función es inválido o la cantidad es menor o igual a cero.
     */
    public static void validateFields(TicketRequestDTO dto) {
        if (dto.getFunctionId() == null || dto.getFunctionId() <= 0) {
            throw new BadRequestException("El ID de la función es inválido.");
        }

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BadRequestException("La cantidad de tickets debe ser mayor a cero.");
        }
    }

    /**
     * Valida que la función tenga suficiente capacidad disponible para la cantidad de tickets solicitados.
     *
     * @param function          La función para la cual se están comprando tickets.
     * @param requestedQuantity Cantidad de tickets solicitados.
     * @throws BadRequestException si no hay suficientes entradas disponibles.
     */
    public static void validateCapacity(Function function, int requestedQuantity) {
        if (function.getAvailableCapacity() < requestedQuantity) {
            throw new BadRequestException("No hay suficientes entradas disponibles. Solo quedan: " + function.getAvailableCapacity() + ".");
        }
    }

    /**
     * Valida que la tarjeta del usuario tenga saldo suficiente para cubrir el total de la compra de tickets.
     *
     * @param card              Tarjeta del usuario.
     * @param requestedQuantity Cantidad de tickets solicitados.
     * @throws BadRequestException si el saldo de la tarjeta es insuficiente.
     */
    public static void validateCardBalance(Card card, int requestedQuantity) {
        double total = TICKET_PRICE * requestedQuantity;
        if (card.getBalance() < total) {
            throw new BadRequestException("Fondos insuficientes en la tarjeta. Total requerido: $" + total);
        }
    }

    /**
     * Valida que el ID del ticket sea válido.
     *
     * @param ticketId ID del ticket a validar.
     * @throws IllegalArgumentException si el ID es nulo o menor o igual a cero.
     */
    public static void validateTicketId(Long ticketId) {
        if (ticketId == null || ticketId <= 0) {
            throw new IllegalArgumentException("El ID del ticket debe ser un número positivo.");
        }
    }


}

