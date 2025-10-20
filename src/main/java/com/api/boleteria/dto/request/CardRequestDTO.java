package com.api.boleteria.dto.request;

import com.api.boleteria.model.enums.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardRequestDTO {

    @NotBlank(message = "El número de tarjeta es obligatorio")
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe tener exactamente 16 dígitos")
    private String cardNumber;

    @NotBlank(message = "El nombre del titular es obligatorio")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$", message = "El nombre solo puede contener letras y espacios")
    private String cardholderName;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Formato inválido. Use MM/YY")
    private String expirationDate;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Formato inválido. Use MM/YY")
    private String issueDate;

    @NotBlank(message = "El código de seguridad es obligatorio")
    @Pattern(regexp = "^[0-9]{3}$", message = "El CVV debe tener exactamente 3 dígitos")
    private String cvv;

    @NotNull(message = "El tipo de tarjeta es obligatorio")
    private CardType cardType;

}
