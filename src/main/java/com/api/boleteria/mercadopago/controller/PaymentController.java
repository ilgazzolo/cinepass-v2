package com.api.boleteria.mercadopago.controller;

import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.mercadopago.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<?> createPreference(@RequestBody PaymentRequestDTO dto) {
        try{
            PaymentResponseDTO response = paymentService.createPreference(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al generar la preferencia. ");

        } finally {

        }
    }
    /*
        // Angular ejemplo
        this.http.post('/api/payments/create', payload).subscribe((res: any) => {
          window.location.href = res.initPoint; // redirige a Mercado Pago
        });
     */

    @GetMapping("/success")
    public String success() { return "Pago aprobado"; }

    @GetMapping("/failure")
    public String failure() { return "Pago fallido"; }

    @GetMapping("/pending")
    public String pending() { return "Pago pendiente"; }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String body,
                                          @RequestParam Map<String,String> params) {
        System.out.println("Webhook recibido: " + body + " params: " + params);
        // Aquí podrías consultar Mercado Pago API y actualizar tu DB.
        return ResponseEntity.ok("OK");
    }

}



