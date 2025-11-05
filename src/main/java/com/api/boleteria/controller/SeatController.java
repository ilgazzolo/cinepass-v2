package com.api.boleteria.controller;

import com.api.boleteria.dto.list.SeatListDTO;
import com.api.boleteria.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
@Validated
@CrossOrigin(origins = {"http://localhost:4200"})
public class SeatController {

    private final SeatService seatService;

    //-------------------------------GET--------------------------------//

    @GetMapping("/function/{functionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<List<SeatListDTO>> getSeatsByFunctionId (@PathVariable Long functionId) {
        List<SeatListDTO> seats = seatService.findSeatsByFunctionId(functionId);
        return ResponseEntity.ok(seats);
    }


}
