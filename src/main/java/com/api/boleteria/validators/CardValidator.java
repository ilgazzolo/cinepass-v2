package com.api.boleteria.validators;

import com.api.boleteria.dto.request.CardRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.model.enums.CardType;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.api.boleteria.service.CardService.MAX_RECHARGE_AMOUNT;
import static com.api.boleteria.service.CardService.MAX_TOTAL_BALANCE;

/**
 * Servicio de validación para tarjetas de crédito.
 *
 * Realiza validaciones personalizadas sobre los datos de la tarjeta, como:
 * - Validación del número mediante el algoritmo de Luhn
 * - Validación de formato y lógica de fechas de emisión y expiración
 * - Validación del código de seguridad (CVV)
 */
@Service
public class CardValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    /**
     * Valida un objeto {@link CardRequestDTO}, verificando:
     * <ul>
     *     <li>Que el número de tarjeta sea válido según el algoritmo de Luhn</li>
     *     <li>Que la fecha de emisión no sea futura</li>
     *     <li>Que la fecha de expiración no esté vencida</li>
     *     <li>Que la fecha de expiración no sea anterior a la de emisión</li>
     *     <li>Que el CVV tenga exactamente 3 dígitos numéricos</li>
     * </ul>
     *
     * @param entity DTO con los datos de la tarjeta a validar.
     * @throws IllegalArgumentException si alguna validación falla, con un mensaje descriptivo.
     */
    public void validateCard(CardRequestDTO entity) {
        if (!isValidLuhn(entity.getCardNumber())) {
            throw new IllegalArgumentException("Número de tarjeta inválido (falló validación Luhn)");
        }

        YearMonth expiration = parseYearMonth(entity.getExpirationDate(), "fecha de expiración");
        YearMonth issue = parseYearMonth(entity.getIssueDate(), "fecha de emisión");

        YearMonth now = YearMonth.now();

        if (issue.isAfter(now)) {
            throw new IllegalArgumentException("La fecha de emisión no puede ser futura");
        }

        if (expiration.isBefore(now)) {
            throw new IllegalArgumentException("La tarjeta ya está vencida");
        }

        if (expiration.isBefore(issue)) {
            throw new IllegalArgumentException("La fecha de expiración no puede ser anterior a la fecha de emisión");
        }

        validateCVV(entity.getCvv(), entity.getCardType());
    }

    /**
     * Convierte una fecha en formato MM/yy a {@link YearMonth}, validando su formato.
     *
     * @param input     Fecha en formato string (MM/yy).
     * @param fieldName Nombre del campo (usado para el mensaje de error).
     * @return Instancia de {@link YearMonth} representando la fecha.
     * @throws IllegalArgumentException si el formato es incorrecto.
     */
    private YearMonth parseYearMonth(String input, String fieldName) {
        try {
            return YearMonth.parse(input, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato inválido para " + fieldName + ", debe ser MM/YY");
        }
    }

    /**
     * Valida el código de seguridad (CVV) de la tarjeta.
     * Debe tener exactamente 3 dígitos numéricos, sin importar el tipo de tarjeta.
     *
     * @param cvv       Código de seguridad.
     * @param cardType  Tipo de tarjeta (no afecta la validación actual).
     * @throws IllegalArgumentException si el CVV es inválido.
     */
    private void validateCVV(String cvv, CardType cardType) {
        if (cvv == null || !cvv.matches("\\d+")) {
            throw new IllegalArgumentException("El código de seguridad debe contener solo dígitos");
        }

        if (cvv.length() != 3) {
            throw new IllegalArgumentException("El CVV debe tener exactamente 3 dígitos");
        }
    }

    /**
     * Valida un número de tarjeta de crédito utilizando el algoritmo de Luhn.
     *
     * @param number Número de tarjeta como string.
     * @return true si el número es válido, false si no.
     */
    public boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Valida que el monto de recarga sea positivo y no supere el límite máximo permitido.
     *
     * @param amount Monto a recargar.
     * @throws BadRequestException si el monto es inválido.
     */
    public static void validateRechargeAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new BadRequestException("El monto debe ser mayor que cero.");
        }
        if (amount > MAX_RECHARGE_AMOUNT) {
            throw new BadRequestException("El monto excede el límite máximo de recarga permitido: $" + MAX_RECHARGE_AMOUNT);
        }
    }

    /**
     * Valida que el nuevo saldo total no supere el máximo permitido.
     *
     * @param currentBalance Saldo actual en la tarjeta.
     * @param amount         Monto a recargar.
     * @throws BadRequestException si el nuevo saldo supera el límite permitido.
     */
    public static void validateTotalBalance(Double currentBalance, Double amount) {
        if (currentBalance + amount > MAX_TOTAL_BALANCE) {
            throw new BadRequestException("El saldo total no puede superar $" + MAX_TOTAL_BALANCE);
        }
    }



}
