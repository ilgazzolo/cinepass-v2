package com.api.boleteria.service;

import com.api.boleteria.dto.detail.CinemaDetailDTO;
import com.api.boleteria.dto.list.SeatListDTO;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.model.Cinema;
import com.api.boleteria.model.Function;
import com.api.boleteria.model.Seat;
import com.api.boleteria.repository.IFunctionRepository;
import com.api.boleteria.repository.ISeatRepository;
import com.api.boleteria.validators.CinemaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Servicio encargado de gestionar las operaciones relacionadas con las butacas (seats)
 * en las funciones de cine.
 * <p>
 * Proporciona métodos para buscar, actualizar y mapear las entidades {@link Seat}
 * asociadas a una función. Permite obtener todas las butacas, filtrar las ocupadas
 * y marcar una butaca como ocupada.
 * </p>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>
 *     List&lt;SeatListDTO&gt; butacas = seatService.findSeatsByFunctionId(5L);
 *     Seat butacaOcupada = seatService.occupySeat(10L);
 * </pre>
 *
 */
@Service
@RequiredArgsConstructor
public class SeatService {

    private final ISeatRepository seatRepository;
    private final IFunctionRepository functionRepo;

    //-------------------------------FIND--------------------------------//

    /**
     * Obtiene todas las butacas asociadas a una función específica.
     *
     * @param functionId ID de la función cuyas butacas se desean obtener.
     * @return una lista de {@link SeatListDTO} que representa todas las butacas de la función indicada.
     * @throws NotFoundException si el ID de la función no existe (manejado indirectamente por el repositorio o el controlador).
     */
    public List<SeatListDTO> findSeatsByFunctionId(Long functionId) {
        List<Seat> seats = seatRepository.findByFunctionId(functionId);
        return seats.stream()
                .map(this::mapToListDTO)
                .toList();
    }

    public Seat findSeatById(Long id) {
        Seat seat = seatRepository.findById(id).orElseThrow(() -> new NotFoundException("Función no encontrada: " + id));
        return seat ;
    }



    /**
     * Obtiene únicamente las butacas ocupadas asociadas a una función específica.
     *
     * @param functionId ID de la función cuyas butacas ocupadas se desean obtener.
     * @return una lista de {@link SeatListDTO} que contiene solo las butacas ocupadas.
     * @throws NotFoundException si el ID de la función no existe (manejado indirectamente por el repositorio o el controlador).
     */
    public List<SeatListDTO> findSeatOcupiedByFunctionId(Long functionId) {
        List<Seat> seats = seatRepository.findByFunctionId(functionId);
        return seats.stream()
                .filter(Seat::getOccupied)
                .map(this::mapToListDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatListDTO> getSeatsByFunction(Long functionId) {
        // Validar existencia de la función
        Function function = functionRepo.findById(functionId)
                .orElseThrow(() -> new NotFoundException("Función no encontrada: " + functionId));

        // Mapear cada Seat a SeatListDTO
        return seatRepository.findByFunction_IdOrderBySeatRowNumberAscSeatColumnNumberAsc(functionId)
                .stream()
                .map(this::mapToListDTO)
                .toList();
    }


    //-------------------------------UPDATE--------------------------------//

    /**
     * Marca una butaca específica como ocupada.
     *
     * @param seatId ID de la butaca que se desea marcar como ocupada.
     * @return la entidad {@link Seat} actualizada con la propiedad {@code occupied} establecida en {@code true}.
     * @throws NotFoundException si no se encuentra la butaca con el ID especificado.
     */
    public Seat occupySeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new NotFoundException("Butaca no encontrada."));
        seat.setOccupied(true);
        return seatRepository.save(seat);
    }

    //-------------------------------MAPS--------------------------------//

    /**
     * Convierte una entidad {@link Seat} en su representación resumida {@link SeatListDTO}.
     *
     * @param seat la entidad {@link Seat} a convertir.
     * @return un {@link SeatListDTO} con los datos básicos de la butaca.
     */
    private SeatListDTO mapToListDTO(Seat seat) {
        return new SeatListDTO(
                seat.getId(),
                seat.getSeatRowNumber(),
                seat.getSeatColumnNumber(),
                seat.getOccupied()
        );
    }

}

