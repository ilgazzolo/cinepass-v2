package com.api.boleteria.mercadopago.controller;

import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.mercadopago.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create_preference")
    public ResponseEntity<PaymentResponseDTO> createPreference(@RequestBody PaymentRequestDTO dto) {
        PaymentResponseDTO response = paymentService.createPreference(dto);
        return ResponseEntity.ok(response);
    }
}



