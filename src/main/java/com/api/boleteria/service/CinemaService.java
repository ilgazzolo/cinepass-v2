package com.api.boleteria.service;

import com.api.boleteria.dto.detail.CinemaDetailDTO;
import com.api.boleteria.dto.list.CinemaListDTO;
import com.api.boleteria.dto.request.CinemaRequestDTO;
import com.api.boleteria.exception.BadRequestException; //
import com.api.boleteria.exception.NotFoundException; //
import com.api.boleteria.model.Cinema; //
import com.api.boleteria.model.enums.ScreenType; //
import com.api.boleteria.repository.ICinemaRepository; //
import com.api.boleteria.repository.IFunctionRepository; //
import com.api.boleteria.validators.CinemaValidator; //
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar operaciones relacionadas con Salas de Cine.
 */
@Service
@RequiredArgsConstructor
public class CinemaService {

    private final ICinemaRepository cinemaRepository;
    private final IFunctionRepository functionRepository;


    //-------------------------------SAVE--------------------------------//

    /**
     * Crea una o más salas (cinemas) a partir de una lista de DTOs.
     *
     * @param request Lista de DTOs con los datos de cada sala a crear.
     * @return Lista de CinemaDetailDTO con la información de las salas creadas.
     * @throws BadRequestException si alguna sala ya existe con el mismo nombre.
     */
    @Transactional
    public CinemaDetailDTO save(CinemaRequestDTO request) {
        List<Cinema> cinemasToSave = new ArrayList<>();


            CinemaValidator.validateFields(request);

            // Verificar si ya existe una sala con el mismo nombre
            if (cinemaRepository.existsByName(request.getName())) { //
                throw new BadRequestException("Ya existe una sala con el nombre: " + request.getName()); //
            }

            Cinema cinema = mapToEntity(request);
            cinema.calculateSeatCapacity();
            cinemaRepository.save(cinema);
            CinemaDetailDTO cinemaDetailDTO = mapToDetailDTO(cinema);

        return cinemaDetailDTO;
    }



    //-------------------------------FIND--------------------------------//

    /**
     * obtiene una sala segun un ID especificado
     * @param id ID de la sala a buscar
     * @return SalaDetail con la informacion de la sala encontrada
     */

    public CinemaDetailDTO findById(Long id) {
        CinemaValidator.validateId(id);
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La sala con ID: " + id + " no fue encontrada. "));
        return mapToDetailDTO(cinema);
    }

    /**
     * muestra todas las salas con un tipo de pantalla en especifico
     * @param screenType tipo de pantalla de la sala a mostrar
     * @return lista de CinemaList con la informacion de las salas encontradas
     */

    public List<CinemaListDTO> findByScreenType(ScreenType screenType) {
        CinemaValidator.validateScreenType(screenType);
        List<CinemaListDTO> list = cinemaRepository.findByScreenType(screenType).stream()
                .map(this::mapToListDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No se encontraron cines con tipo de pantalla: " + screenType);
        }

        return list;
    }

    /**
     * obtiene las salas segun un estado especificado
     * @param enabled estado de las salas a mostrar
     * @return CinemaList con la informacion de las salas encontradas
     */

    public List<CinemaDetailDTO> findByEnabledRoom(boolean enabled) {
        CinemaValidator.validateEnabled(enabled);
        List<CinemaDetailDTO> list = cinemaRepository.findByEnabled(enabled).stream()
                .map(this::mapToDetailDTO)
                .toList();



        return list;
    }

    /**
     * obtiene las salas cuya capacidad de asientos sea mayor al valor recibido como parámetro.
     * @param seatCapacity capacidad de asientos especificada
     * @return CinemaList con la informacion de las salas encontradas
     */

    public List<CinemaListDTO> findBySeatCapacity(Integer seatCapacity) {
        List<CinemaListDTO> list = cinemaRepository.findBySeatCapacityGreaterThan(seatCapacity).stream()
                .map(this::mapToListDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No se encontraron cines con capacidad mayor a: " + seatCapacity);
        }

        return list;
    }



    //-------------------------------UPDATE--------------------------------//

    /**
     * actualiza una sala, segun un ID especificado
     * @param id ID de la sala a actualizar
     * @param entity DTO con los cambios realizados
     * @return CinemaDetail con la informacion de la sala actualizada
     */

    public CinemaDetailDTO updateById(Long id, CinemaRequestDTO entity) {
        CinemaValidator.validateFields(entity);
        CinemaValidator.validateId(id);

        // Verificar si el nuevo nombre ya existe en otra sala (excluyendo la sala actual)
        if (cinemaRepository.existsByNameAndIdNot(entity.getName(), id)) { //
            throw new BadRequestException("Ya existe otra sala con el nombre: " + entity.getName()); //
        }

        return cinemaRepository.findById(id)
                .map(c -> {
                    c.setName(entity.getName());
                    c.setScreenType(entity.getScreenType());
                    c.setAtmos(entity.getAtmos());
                    c.setRowSeat(entity.getRowSeat());
                    c.setColumnSeat(entity.getColumnSeat());
                    c.calculateSeatCapacity();
                    c.setEnabled(entity.getEnabled());
                    c.setPrice(entity.getPrice());
                    Cinema updated = cinemaRepository.save(c);
                    return mapToDetailDTO(updated);
                })
                .orElseThrow(() -> new NotFoundException("La sala con ID: " + id + " no fue encontrada. "));
    }

    /**
     * Actualiza solo el atributo 'enabled' de una sala según su ID.
     *
     * @param id ID de la sala a modificar.
     * @param enabled Nuevo valor para el atributo 'enabled'.
     * @return CinemaDetailDTO con la información actualizada de la sala.
     * @throws NotFoundException si la sala con el ID especificado no existe.
     */
    public CinemaDetailDTO updateEnabledById(Long id, boolean enabled) {
        CinemaValidator.validateEnabled(enabled);
        CinemaValidator.validateId(id);

        return cinemaRepository.findById(id)
                .map(c -> {
                    c.setEnabled(enabled);
                    Cinema updated = cinemaRepository.save(c);
                    return mapToDetailDTO(updated);
                })
                .orElseThrow(() -> new NotFoundException("La sala con ID: " + id + " no fue encontrada."));
    }







    //-------------------------------DELETE--------------------------------//

    /**
     * elimina una sala segun un ID especificado
     * @param id ID de la sala a eliminar
     */
    public void deleteById(Long id) {
        CinemaValidator.validateId(id);
        if (!cinemaRepository.existsById(id)) {
            throw new NotFoundException("La sala con ID: " + id + " no fue encontrada. ");
        }
        cinemaRepository.deleteById(id);
    }

    public List<CinemaListDTO> findAll() {
        List<CinemaListDTO> list = cinemaRepository.findAll()
                .stream()
                .map(this::mapToListDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No hay cines registrados.");
        }

        return list;
    }



    //-------------------------------MAPS--------------------------------//
    /**
     * Convierte una entidad Cinema en un DTO de listado.
     * @param cinema entidad Cinema a convertir
     * @return CinemaListDTO con los datos resumidos de la sala
     */
    private CinemaListDTO mapToListDTO(Cinema cinema) {
        return new CinemaListDTO(
                cinema.getId(),
                cinema.getName(),
                cinema.getSeatCapacity(),
                cinema.getEnabled()
        );
    }

    /**
     * Convierte una entidad Cinema en un DTO de detalle.
     * @param cinema entidad Cinema a convertir
     * @return CinemaDetailDTO con los datos detallados de la sala
     */
    private CinemaDetailDTO mapToDetailDTO(Cinema cinema) {
        return new CinemaDetailDTO(
                cinema.getId(),
                cinema.getName(),
                cinema.getScreenType(),
                cinema.getAtmos(),
                cinema.getRowSeat(),
                cinema.getColumnSeat(),
                cinema.getSeatCapacity(),
                cinema.getEnabled(),
                cinema.getPrice()
        );
    }

    private Cinema mapToEntity(CinemaRequestDTO dto) {
        Cinema cinema = new Cinema();
        cinema.setName(dto.getName());
        cinema.setScreenType(dto.getScreenType());
        cinema.setAtmos(dto.getAtmos());
        cinema.setRowSeat(dto.getRowSeat());
        cinema.setColumnSeat(dto.getColumnSeat());
        cinema.calculateSeatCapacity();
        cinema.setEnabled(dto.getEnabled());
        cinema.setPrice(dto.getPrice());
        return cinema;
    }
}