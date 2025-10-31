package com.api.boleteria.mercadopago.controller;

import com.api.boleteria.log.PaymentLog;
import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.mercadopago.service.PaymentService;
import com.api.boleteria.model.Payment;
import com.mercadopago.client.payment.PaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


/**
 * REST controller responsible for managing payment-related endpoints.
 * Handles creation of payment preferences, success/failure/pending callbacks,
 * and webhook notifications from Mercado Pago.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Creates a Mercado Pago payment preference using the data provided by the client.
     * Accessible only to users with roles {@code ADMIN} or {@code CLIENT}.
     *
     * @param dto The {@link PaymentRequestDTO} containing details for creating the payment preference.
     * @return A {@link ResponseEntity} containing the {@link PaymentResponseDTO} on success,
     *         or an error message if something goes wrong.
     */
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
            return ResponseEntity.internalServerError().body("Error generating payment preference.");
        }
    }


    /**
     * Endpoint called when a payment is successfully completed.
     *
     * @return A confirmation message indicating successful payment.
     */
    @GetMapping("/success")
    public String success() { return "Payment success"; }

    /**
     * Endpoint called when a payment fails.
     *
     * @return A message indicating payment failure.
     */
    @GetMapping("/failure")
    public String failure() { return "Payment failure"; }

    /**
     * Endpoint called when a payment is still pending.
     *
     * @return A message indicating the payment is pending.
     */
    @GetMapping("/pending")
    public String pending() { return "Payment pending"; }

    /**
     * Receives webhook notifications from Mercado Pago.
     * Extracts the payment ID and delegates processing to the {@link PaymentService}.
     *
     * @param payload The notification payload containing payment information.
     * @return A {@link ResponseEntity} indicating whether the notification was processed successfully.
     */
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




