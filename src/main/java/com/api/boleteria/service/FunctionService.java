package com.api.boleteria.service;

import com.api.boleteria.dto.detail.FunctionDetailDTO;
import com.api.boleteria.dto.detail.MovieDetailDTO;
import com.api.boleteria.dto.list.FunctionListDTO;
import com.api.boleteria.dto.request.FunctionRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.model.*;
import com.api.boleteria.model.enums.ScreenType;
import com.api.boleteria.repository.ICinemaRepository;
import com.api.boleteria.repository.IFunctionRepository;
import com.api.boleteria.validators.CinemaValidator;
import com.api.boleteria.validators.FunctionValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private final MovieService movieService;


    //-------------------------------SAVE--------------------------------//

    /**
     * Crea una función, validando que no exista en la misma sala y horario,
     * que la fecha no supere los dos años y que no haya solapamientos.
     *
     * @param entity DTO con la información de la nueva función.
     * @return FunctionDetailDTO con la información de la función creada.
     * @throws BadRequestException si la función no cumple las validaciones.
     * @throws NotFoundException si la sala o la película no existen.
     */
    @Transactional
    public FunctionDetailDTO create(FunctionRequestDTO entity) {
        // Validaciones
        FunctionValidator.validateFields(entity);
        FunctionValidator.validateMaxTwoYears(entity);

        if (functionRepo.existsByCinemaIdAndShowtime(entity.getCinemaId(), entity.getShowtime())) {
            throw new BadRequestException("Ya existe una función para la sala " + entity.getCinemaId() +
                    " en el horario " + entity.getShowtime());
        }

        Cinema cinema = cinemaRepo.findById(entity.getCinemaId())
                .orElseThrow(() -> new NotFoundException("No existe la sala con ID: " + entity.getCinemaId()));
        FunctionValidator.validateEnabledCinema(cinema);

        MovieDetailDTO movie;
        try {
            movie = movieService.getMovieById(entity.getMovieId());
            if (movie == null) {
                throw new NotFoundException("No existe la película con ID: " + entity.getMovieId());
            }
        } catch (IOException e) {
            throw new NotFoundException("No existe la película con ID: " + entity.getMovieId());
        }

        List<Function> functionsInTheCinema = functionRepo.findByCinemaId(entity.getCinemaId());
        FunctionValidator.validateSchedule(entity, movie, functionsInTheCinema);

        // Crear la función
        Function function = mapToEntity(entity, cinema, movie);
        function.setCinema(cinema);
        function.setMovieId(movie.id());
        function.setMovieName(movie.title());
        function.setRunTime(movie.runtime());

        // Crear los asientos para esta función
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= cinema.getRowSeat(); row++) {
            for (int col = 1; col <= cinema.getColumnSeat(); col++) {
                Seat seat = new Seat();
                seat.setSeatRowNumber(row);
                seat.setSeatColumnNumber(col);
                seat.setOccupied(false);
                seat.setFunction(function);
                seats.add(seat);
            }
        }

        function.setSeats(seats);

        // Guardar función y devolver DTO
        Function saved = functionRepo.save(function);
        return mapToDetailDTO(saved);
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

        MovieDetailDTO movie;

        try {
            movie = movieService.getMovieById(movieId);
            if (movie == null) {
                throw new NotFoundException("No existe la película con ID: " + movieId);
            }
        } catch (IOException e) {
            throw new NotFoundException("No existe la película con ID: " + movieId);
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

        MovieDetailDTO movie;

        try {
            movie = movieService.getMovieById(entity.getMovieId());
            if (movie == null) {
                throw new NotFoundException("No existe la película con ID: " + entity.getMovieId());
            }
        } catch (IOException e) {
            throw new NotFoundException("No existe la película con ID: " + entity.getMovieId());
        }

        List<Function> functionsInCinema = functionRepo.findByCinemaId(entity.getCinemaId());
        FunctionValidator.validateSchedule(entity, movie, functionsInCinema);

        function.setShowtime(entity.getShowtime());
        function.setCinema(cinema);
        function.setMovieId(movie.id());
        function.setMovieName(movie.title());
        function.setAvailableCapacity(cinema.getSeatCapacity());
        function.setRunTime(movie.runtime());

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
                function.getMovieId(),
                function.getMovieName(),
                function.getAvailableCapacity(),
                function.getRunTime()
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
                function.getMovieId(),
                function.getMovieName(),
                function.getRunTime()
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
    private Function mapToEntity(FunctionRequestDTO entity, Cinema cinema, MovieDetailDTO movie) {
        Function function = new Function();
        function.setShowtime(entity.getShowtime());
        function.setCinema(cinema);
        function.setAvailableCapacity(cinema.getSeatCapacity());
        function.setMovieId(movie.id());
        function.setMovieName(movie.title());
        return function;
    }


}
