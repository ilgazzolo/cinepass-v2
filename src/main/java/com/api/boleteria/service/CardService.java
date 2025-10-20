package com.api.boleteria.service;

import com.api.boleteria.dto.detail.CardDetailDTO;
import com.api.boleteria.dto.request.CardRequestDTO;
import com.api.boleteria.exception.BadRequestException; //
import com.api.boleteria.exception.NotFoundException; //
import com.api.boleteria.model.Card; //
import com.api.boleteria.model.User; //
import com.api.boleteria.repository.ICardRepository; //
import com.api.boleteria.repository.IUserRepository;
import com.api.boleteria.validators.CardValidator; //
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Servicio para la gestión de tarjetas de los usuarios.
 *
 * Proporciona operaciones para crear, actualizar, recargar saldo,
 * obtener detalles y eliminar la tarjeta asociada al usuario autenticado.
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final ICardRepository cardRepository;
    private final CardValidator cardValidator;
    private final UserService userService;
    private final IUserRepository userRepo;

    public static final double MAX_RECHARGE_AMOUNT = 20000.0;
    public static final double MAX_TOTAL_BALANCE = 1000000.0;


    //-------------------------------SAVE--------------------------------//

    /**
     * Crea una nueva tarjeta para el usuario autenticado.
     *
     * @param dto Datos necesarios para crear la tarjeta.
     * @return DTO con el detalle de la tarjeta creada.
     * @throws BadRequestException si el número de tarjeta ya está en uso.
     */
    public CardDetailDTO save(CardRequestDTO dto) {
        cardValidator.validateCard(dto);

        // Validar si el número de tarjeta ya existe globalmente
        if (cardRepository.existsByCardNumber(dto.getCardNumber())) { //
            throw new BadRequestException("El número de tarjeta '" + dto.getCardNumber() + "' ya está registrado."); //
        }

        Card card = mapToEntity(dto);
        Card saved = cardRepository.save(card);
        return mapToDetailDTO(saved);
    }



    //-------------------------------GET/FIND--------------------------------//

    /**
     * Obtiene el saldo actual de la tarjeta del usuario autenticado.
     *
     * @return Saldo actual.
     */
    public Double getBalance() {
        User user = userService.findAuthenticatedUser();
        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("El usuario: " + user.getUsername() + " no tiene una tarjeta registrada."));
        return card.getBalance();
    }

    /**
     * Obtiene el detalle de la tarjeta del usuario autenticado.
     *
     * @return DTO con el detalle de la tarjeta.
     */
    public CardDetailDTO findFromAuthenticatedUser() {
        User user = userService.findAuthenticatedUser();

        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("No se encontró tarjeta para el usuario: " + user.getUsername()));

        return mapToDetailDTO(card);
    }



    //-------------------------------UPDATE--------------------------------//

    /**
     * Recarga saldo a la tarjeta del usuario autenticado.
     *
     * @param amount Monto a recargar.
     * @return DTO con el detalle actualizado.
     */
    public CardDetailDTO rechargeBalance(Double amount) {
       CardValidator.validateRechargeAmount(amount);

        User user = userService.findAuthenticatedUser();

        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("El usuario: " + user.getUsername() + " no tiene una tarjeta registrada."));

       CardValidator.validateTotalBalance(card.getBalance(), amount);

        card.setBalance(card.getBalance() + amount);
        cardRepository.save(card);

        return mapToDetailDTO(card);
    }




//-------------------------------DELETE--------------------------------//

    /**
     * Elimina la tarjeta asociada al usuario autenticado.
     *
     * Obtiene el usuario actualmente autenticado y busca la tarjeta
     * asociada a ese usuario. Si no se encuentra ninguna tarjeta, lanza
     * una excepción NotFoundException. Si existe, elimina la tarjeta del repositorio.
     */
    public void deleteFromAuthenticatedUser() {
        User user = userService.findAuthenticatedUser();

        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("No se encontró tarjeta para el usuario: " + user.getUsername()));

        user.setCard(null);
        userRepo.save(user);

        cardRepository.deleteById(card.getId());
    }


//-------------------------------MAPS--------------------------------//

    /**
     * Convierte un objeto CardRequestDTO a una entidad Card.
     *
     * Inicializa la tarjeta con los datos del DTO, balance en 0.0 y
     * asocia la tarjeta al usuario autenticado.
     *
     * @param dto Objeto con los datos de la tarjeta a crear.
     * @return La entidad Card creada a partir del DTO.
     */
    private Card mapToEntity(CardRequestDTO dto) {
        Card card = new Card();
        card.setCardNumber(dto.getCardNumber());
        card.setCardholderName(dto.getCardholderName());
        card.setExpirationDate(dto.getExpirationDate());
        card.setIssueDate(dto.getIssueDate());
        card.setCvv(dto.getCvv());
        card.setCardType(dto.getCardType());
        card.setBalance(0.0);
        card.setUser(userService.findAuthenticatedUser());
        return card;
    }

    /**
     * Convierte una entidad Card a un DTO de detalle CardDetailDTO.
     *
     * Transforma la entidad en un objeto que contiene la información
     * necesaria para la presentación o respuesta, incluyendo el ID
     * del usuario asociado.
     *
     * @param card Entidad Card a convertir.
     * @return DTO con los detalles de la tarjeta.
     */
    private CardDetailDTO mapToDetailDTO(Card card) {
        return new CardDetailDTO(
                card.getId(),
                card.getCardNumber(),
                card.getCardholderName(),
                card.getExpirationDate(),
                card.getIssueDate(),
                card.getCardType().toString().toUpperCase(),
                card.getBalance(),
                card.getUser().getId()
        );
    }

}