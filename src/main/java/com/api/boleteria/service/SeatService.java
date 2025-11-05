package com.api.boleteria.service;

import com.api.boleteria.dto.list.SeatListDTO;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.model.Seat;
import com.api.boleteria.repository.ISeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


/**
 * Servicio para gestionar operaciones relacionadas con las butacas de Salas.
 */
@Service
@RequiredArgsConstructor
public class SeatService {

    private final ISeatRepository seatRepository;

    //-------------------------------FIND--------------------------------//

    public List<SeatListDTO> findSeatsByFunctionId(Long functionId) {
        List<Seat> seats = seatRepository.findByFunctionId(functionId);
        return seats.stream()
                .map(this::mapToListDTO)
                .toList();
    }


    //-------------------------------UPDATE--------------------------------//

    public Seat occupySeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new NotFoundException("Butaca no encontrada."));
        seat.setOccupied(true);
        return seatRepository.save(seat);
    }


    //-------------------------------MAPS--------------------------------//

    /**
     * Convierte una entidad Seat en un DTO de lista.
     * @param seat entidad Seat
     * @return SeatListDTO con los datos resumidos de la butaca
     */
    private SeatListDTO mapToListDTO(Seat seat) {
        return new SeatListDTO(
                seat.getId(),
                seat.getRowNumber(),
                seat.getColumnNumber(),
                seat.getOccupied()
        );
    }


}
