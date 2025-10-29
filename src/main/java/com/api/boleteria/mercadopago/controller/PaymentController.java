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
        }
    }
    /*
        // Angular ejemplo
        this.http.post('/api/payments/create', payload).subscribe((res: any) => {
          window.location.href = res.initPoint; // redirige a Mercado Pago
        });
     */

    @GetMapping("/success")
    public String success() { return "Payment success"; }

    @GetMapping("/failure")
    public String failure() { return "Payment failure"; }

    @GetMapping("/pending")
    public String pending() { return "Payment pending"; }

    @PostMapping("/notification")
    public ResponseEntity<String> receiveNotification(@RequestBody Map<String, Object> payload) {
        try {
            // El campo "data.id" contiene el ID del pago de Mercado Pago
            if (payload.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String mpPaymentId = String.valueOf(data.get("id"));

                // Llamamos al servicio para procesar el pago
                paymentService.processWebhookNotification(mpPaymentId);
            }

            return ResponseEntity.ok("Notification received");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing notification: " + e.getMessage());
        }
    }
}



