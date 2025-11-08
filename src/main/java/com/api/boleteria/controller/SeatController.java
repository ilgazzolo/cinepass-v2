package com.api.boleteria.controller;

import com.api.boleteria.dto.list.SeatListDTO;
import com.api.boleteria.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de asientos (Seat) en el sistema.
 * Proporciona endpoints para consultar los asientos de una función,
 * tanto todos los asientos como únicamente los ocupados.
 *
 * <p>Los métodos están protegidos mediante Spring Security, permitiendo el acceso
 * a usuarios con rol ADMIN o CLIENT.</p>
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
@Validated
@CrossOrigin(origins = {"http://localhost:4200"})
public class SeatController {

    private final SeatService seatService;

    //-------------------------------GET--------------------------------//

    /**
     * Obtiene todos los asientos asociados a una función específica.
     *
     * @param functionId ID de la función cuyos asientos se desean consultar.
     * @return {@link ResponseEntity} con una lista de {@link SeatListDTO} representando
     *         los asientos de la función.
     *
     * @apiNote Este endpoint puede ser accedido por usuarios con rol ADMIN o CLIENT.
     */
    @GetMapping("/function/{functionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<List<SeatListDTO>> getSeatsByFunctionId(@PathVariable Long functionId) {
        List<SeatListDTO> seats = seatService.findSeatsByFunctionId(functionId);
        return ResponseEntity.ok(seats);
    }

    /**
     * Obtiene únicamente los asientos ocupados de una función específica.
     *
     * @param functionId ID de la función cuyos asientos ocupados se desean consultar.
     * @return {@link ResponseEntity} con una lista de {@link SeatListDTO} representando
     *         los asientos actualmente ocupados en la función.
     *
     * @apiNote Este endpoint puede ser accedido por usuarios con rol ADMIN o CLIENT.
     */
    @GetMapping("/ocupied/function/{functionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<List<SeatListDTO>> getSeatsOcupiedByFunctionId(@PathVariable Long functionId) {
        List<SeatListDTO> seats = seatService.findSeatOcupiedByFunctionId(functionId);
        return ResponseEntity.ok(seats);
    }
}

