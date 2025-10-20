package com.api.boleteria.service;

import com.api.boleteria.dto.detail.FunctionDetailDTO;
import com.api.boleteria.dto.list.FunctionListDTO;
import com.api.boleteria.dto.request.FunctionRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.model.*;
import com.api.boleteria.model.enums.ScreenType;
import com.api.boleteria.repository.ICardRepository;
import com.api.boleteria.repository.ICinemaRepository;
import com.api.boleteria.repository.IFunctionRepository;
import com.api.boleteria.repository.IMovieRepository;
import com.api.boleteria.validators.CinemaValidator;
import com.api.boleteria.validators.FunctionValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar operaciones relacionadas con Funciones.
 */
@Service
@RequiredArgsConstructor
public class FunctionService {

    private final IFunctionRepository functionRepo;
    private final ICinemaRepository cinemaRepo;
    private final IMovieRepository movieRepo;
    private final ICardRepository cardRepo;


    //-------------------------------SAVE--------------------------------//

    /**
     * Crea una o varias funciones, validando para cada una que no exista función en la misma sala y horario,
     * que la fecha no supere los dos años, y que no haya solapamientos.
     *
     * @param entities Lista de DTOs con la información de las nuevas funciones.
     * @return Lista de FunctionDetailDTO con la información de las funciones creadas.
     * @throws BadRequestException si alguna función no cumple las validaciones.
     * @throws NotFoundException si alguna sala o película no existe.
     */
    @Transactional
    public List<FunctionDetailDTO> createAll(List<FunctionRequestDTO> entities) {
        List<FunctionDetailDTO> createdFunctions = new ArrayList<>();

        for (FunctionRequestDTO entity : entities) {
            FunctionValidator.validateFields(entity);

            if (functionRepo.existsByCinemaIdAndShowtime(entity.getCinemaId(), entity.getShowtime())) {
                throw new BadRequestException("Ya existe una función para la sala " + entity.getCinemaId() + " en el horario " + entity.getShowtime());
            }

            FunctionValidator.validateMaxTwoYears(entity);

            Cinema cinema = cinemaRepo.findById(entity.getCinemaId())
                    .orElseThrow(() -> new NotFoundException("No existe la sala con ID: " + entity.getCinemaId()));
            FunctionValidator.validateEnabledCinema(cinema);

            Movie movie = movieRepo.findById(entity.getMovieId())
                    .orElseThrow(() -> new NotFoundException("No existe la película con ID: " + entity.getMovieId()));

            List<Function> functionsInTheCinema = functionRepo.findByCinemaId(entity.getCinemaId());
            FunctionValidator.validateSchedule(entity, movie, functionsInTheCinema);

            Function function = mapToEntity(entity, cinema, movie);

            function.setCinema(cinema);
            function.setMovie(movie);

            Function saved = functionRepo.save(function);



            createdFunctions.add(mapToDetailDTO(saved));
        }

        return createdFunctions;
    }





    //-------------------------------FIND--------------------------------//

    /**
     * Muestra todas las funciones.
     *
     * @return Lista de FunctionListDTO con la información de las funciones encontradas.
     * @throws NotFoundException si no hay funciones cargadas en el sistema.
     */
    public List<FunctionListDTO> findAll() {
        List<Function> functions = functionRepo.findAll()
                .stream()
                .filter(f -> Boolean.TRUE.equals(f.getCinema().getEnabled()))
                .toList();

        if (functions.isEmpty()) {
            throw new NotFoundException("No hay funciones cargadas en el sistema.");
        }

        return functions.stream()
                .map(this::mapToListDTO)
                .toList();
    }


    /**
     * obtiene las funciones segun un ID especificado
     * @param id de la funcion a buscar
     * @return Function Detail con la informacion de la funcion encontrada
     */
    public FunctionDetailDTO findById(Long id) {
        FunctionValidator.validateId(id);

        Function function = functionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("La función con ID: " + id + " no fue encontrada."));

        if (!Boolean.TRUE.equals(function.getCinema().getEnabled())) {
            throw new NotFoundException("La sala de la función con ID " + id + " está deshabilitada.");
        }

        return mapToDetailDTO(function);
    }


    /**
     * Muestra solo las próximas funciones disponibles de una película, según su ID.
     *
     * Valida que el ID de la película sea mayor a 0 y que exista en la base de datos.
     * Solo se devuelven funciones con capacidad disponible y cuya fecha de inicio
     * sea posterior al momento actual.
     *
     * @param movieId ID de la película que se desea mostrar sus funciones.
     * @return Lista de FunctionListDTO con la información de las funciones encontradas.
     * @throws IllegalArgumentException si el ID proporcionado no es válido.
     * @throws NotFoundException si no existe una película con el ID especificado o si no hay funciones disponibles.
     */
    public List<FunctionDetailDTO> findByMovieIdAndAvailableCapacity(Long movieId) {
        FunctionValidator.validateMovieId(movieId);

        if (!movieRepo.existsById(movieId)) {
            throw new NotFoundException("La película con ID " + movieId + " no fue encontrada.");
        }

        List<Function> functions = functionRepo
                .findByMovieIdAndAvailableCapacityGreaterThanAndShowtimeAfterAndCinema_EnabledTrue(
                        movieId, 0, LocalDateTime.now());

        if (functions.isEmpty()) {
            throw new NotFoundException("No hay funciones disponibles para la película con ID " + movieId);
        }

        return functions.stream()
                .map(this::mapToDetailDTO)
                .toList();
    }

    /**
     * Muestra las funciones según un tipo de pantalla especificado.
     *
     * @param screenType tipo de pantalla especificado.
     * @return Lista de FunctionListDTO con las funciones encontradas.
     * @throws NotFoundException si no hay funciones disponibles para el tipo de pantalla.
     */
    public List<FunctionDetailDTO> findByScreenType(ScreenType screenType) {
        CinemaValidator.validateScreenType(screenType);

        List<Function> functions = functionRepo
                .findByCinema_ScreenTypeAndAvailableCapacityGreaterThanAndShowtimeAfterAndCinema_EnabledTrue(
                        screenType, 0, LocalDateTime.now());

        if (functions.isEmpty()) {
            throw new NotFoundException("No hay funciones disponibles para el tipo de pantalla: " + screenType);
        }

        return functions.stream()
                .map(this::mapToDetailDTO)
                .toList();
    }

    /**
     * Muestra las funciones disponibles de una sala específica según su ID.
     *
     * Solo se devuelven funciones si:
     * - La sala existe.
     * - La sala está habilitada.
     * - La función tiene capacidad disponible.
     * - La función tiene una fecha posterior a la actual.
     *
     * @param cinemaId ID de la sala a filtrar funciones.
     * @return Lista de FunctionDetailDTO con la información de las funciones encontradas.
     * @throws NotFoundException si la sala no existe, está deshabilitada o no hay funciones disponibles.
     */
    public List<FunctionDetailDTO> findByCinemaId(Long cinemaId) {
        FunctionValidator.validateCinemaId(cinemaId);

        Cinema cinema = cinemaRepo.findById(cinemaId)
                .orElseThrow(() -> new NotFoundException("No se encontró la sala con ID " + cinemaId));

        if (!Boolean.TRUE.equals(cinema.getEnabled())) {
            throw new NotFoundException("La sala con ID " + cinemaId + " está deshabilitada.");
        }

        List<Function> functions = functionRepo
                .findByCinemaIdAndAvailableCapacityGreaterThanAndShowtimeAfter(
                        cinemaId, 0, LocalDateTime.now());

        if (functions.isEmpty()) {
            throw new NotFoundException("No hay funciones disponibles para la sala con ID " + cinemaId);
        }

        return functions.stream()
                .map(this::mapToDetailDTO)
                .toList();
    }




    //-------------------------------UPDATE--------------------------------//
    /**
     * Actualiza una función según el ID especificado.
     *
     * @param id ID de la función a modificar.
     * @param entity Objeto DTO con los campos modificados.
     * @return FunctionDetailDTO con la información de la función actualizada.
     * @throws NotFoundException si la función, sala o película no existen.
     * @throws BadRequestException si hay conflictos de horario, validaciones o restricciones.
     */
    public FunctionDetailDTO updateById(Long id, FunctionRequestDTO entity) {
        FunctionValidator.validateId(id);
        FunctionValidator.validateFields(entity);
        FunctionValidator.validateMaxTwoYears(entity);

        Function function = functionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("La función con ID: " + id + " no fue encontrada."));

        if (functionRepo.existsByCinemaIdAndShowtime(entity.getCinemaId(), entity.getShowtime())) {
            throw new BadRequestException("Ya existe una función para esa sala en ese horario.");
        }

        Cinema cinema = cinemaRepo.findById(entity.getCinemaId())
                .orElseThrow(() -> new NotFoundException("No existe la sala ingresada."));
        FunctionValidator.validateEnabledCinema(cinema);

        Movie movie = movieRepo.findById(entity.getMovieId())
                .orElseThrow(() -> new NotFoundException("No existe la película ingresada."));

        List<Function> functionsInCinema = functionRepo.findByCinemaId(entity.getCinemaId());
        FunctionValidator.validateSchedule(entity, movie, functionsInCinema);

        function.setShowtime(entity.getShowtime());
        function.setCinema(cinema);
        function.setMovie(movie);
        function.setAvailableCapacity(cinema.getSeatCapacity());

        Function updated = functionRepo.save(function);
        return mapToDetailDTO(updated);
    }



    //-------------------------------DELETE--------------------------------//
    /**
     * Elimina una función según un ID especificado.
     * <p>
     * Antes de eliminar, reintegra el saldo correspondiente en la tarjeta de cada usuario
     * que tenga tickets asociados a la función.
     * </p>
     *
     * @param id ID de la función a eliminar.
     * @throws NotFoundException si no se encuentra la función con el ID especificado
     *         o si algún usuario asociado no tiene tarjeta registrada.
     */
    @Transactional
    public void deleteById(Long id) {
        Function function = functionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("La función con ID: " + id + " no fue encontrada."));

        List<Ticket> tickets = function.getTickets();

        for (Ticket ticket : tickets) {
            User user = ticket.getUser();
            Card card = cardRepo.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("El usuario " + user.getUsername() + " no tiene una tarjeta registrada."));

            // Reintegrar saldo
            card.setBalance(card.getBalance() + ticket.getTicketPrice());
            cardRepo.save(card);
        }

        // Eliminar función junto con sus tickets (gracias a cascade y orphanRemoval)
        functionRepo.delete(function);
    }


    //-------------------------------MAPS--------------------------------//

    /**
     * Convierte una entidad Function en un DTO de detalle.
     * @param function entidad Function
     * @return FunctionDetailDTO con los datos detallados de la función
     */
    private FunctionDetailDTO mapToDetailDTO(Function function) {
        return new FunctionDetailDTO(
                function.getId(),
                function.getShowtime().format(DateTimeFormatter.ISO_DATE_TIME),
                function.getCinema().getId(),
                function.getCinema().getName(),
                function.getMovie().getId(),
                function.getMovie().getTitle(),
                function.getAvailableCapacity()
        );
    }

    /**
     * Convierte una entidad Function en un DTO de lista.
     * @param function entidad Function
     * @return FunctionListDTO con los datos resumidos de la función
     */
    private FunctionListDTO mapToListDTO(Function function) {
        return new FunctionListDTO(
                function.getId(),
                function.getShowtime().toLocalDate(),
                function.getShowtime().toLocalTime(),
                function.getCinema().getId(),
                function.getMovie().getId()
        );
    }

    /**
     * Convierte un FunctionRequestDTO en una entidad Function.
     *
     * Asocia la función al cine y a la película proporcionados, y
     * asigna la capacidad disponible inicial igual a la capacidad de asientos del cine.
     *
     * @param entity DTO con los datos de la función a crear.
     * @param cinema Entidad Cinema asociada a la función.
     * @param movie Entidad Movie asociada a la función.
     * @return Entidad Function creada a partir del DTO y las entidades asociadas.
     */
    private Function mapToEntity(FunctionRequestDTO entity, Cinema cinema, Movie movie) {
        Function function = new Function();
        function.setShowtime(entity.getShowtime());
        function.setCinema(cinema);
        function.setAvailableCapacity(cinema.getSeatCapacity());
        function.setMovie(movie);
        return function;
    }


}
