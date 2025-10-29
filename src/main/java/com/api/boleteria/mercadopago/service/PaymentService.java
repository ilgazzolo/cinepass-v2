package com.api.boleteria.mercadopago.service;

import com.api.boleteria.log.PaymentLog;
import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.model.Payment;
import com.api.boleteria.model.enums.StatusPayment;
import com.api.boleteria.repository.IPaymentLogRepository;
import com.api.boleteria.repository.IPaymentRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.payment.PaymentStatus;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.MercadoPagoConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class responsible for managing payment operations and interactions
 * with the Mercado Pago API. It handles the creation of payment preferences,
 * status updates, and webhook notifications, while maintaining logs of all
 * payment-related events for audit and debugging purposes.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IPaymentRepository paymentRepository;
    private final IPaymentLogRepository paymentLogRepository;

    /**
     * Creates a payment preference in Mercado Pago using the provided payment details.
     * This method configures the item, return URLs, and notification URL,
     * then sends a request to Mercado Pago to generate the payment preference.
     * It also logs the creation of the preference for tracking purposes.
     *
     * @param dto The {@link PaymentRequestDTO} containing payment information such as title, description, quantity, price, and user email.
     * @return A {@link PaymentResponseDTO} containing the preference ID and sandbox payment URL to redirect the user.
     * @throws RuntimeException if any error occurs during the creation of the payment preference.
     */
    public PaymentResponseDTO createPreference(PaymentRequestDTO dto) {
        try {
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));

            // Crear ítem de la preferencia
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .quantity(dto.getQuantity())
                    .currencyId("ARS")
                    .unitPrice(dto.getUnitPrice())
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://larhonda-progravid-caressively.ngrok-free.dev/success")
                    .pending("https://larhonda-progravid-caressively.ngrok-free.dev/pending")
                    .failure("https://larhonda-progravid-caressively.ngrok-free.dev/failure")
                    .build();

            // Crear preferencia con back_urls y auto_return
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(backUrls)
                    .notificationUrl("https://larhonda-progravid-caressively.ngrok-free.dev/api/payments/notification")
                    .autoReturn("approved")
                    .build();


            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);


            // Registrar log del intento
            PaymentLog log = new PaymentLog();
            log.setStatus("PREFERENCE_CREATED");
            log.setUserEmail(dto.getUserEmail());
            log.setTimestamp(LocalDateTime.now());
            paymentLogRepository.save(log);

            return new PaymentResponseDTO(
                    preference.getId(),        //  ID único de la preferencia
                    //        preference.getInitPoint()  //  devuelve URL para redirigir al pago
                    preference.getSandboxInitPoint()  // devuelve URL para redirigir al pago para pruebas
            );


        } catch (Exception e) {
            throw new RuntimeException("Error creating payment preference: " + e.getMessage());
        }
    }

    /**
     * Updates the payment status in the database using the Mercado Pago payment ID.
     * If the status provided is invalid, it defaults to PENDING.
     * Also creates a log entry recording the update operation.
     *
     * @param mpPaymentId The Mercado Pago payment ID associated with the transaction.
     * @param mpStatus The current payment status (e.g., approved, pending, rejected).
     * @param userEmail The email of the user associated with the payment.
     */
    public void updatePaymentStatus(String mpPaymentId, String mpStatus, String userEmail) {
        Payment payment = paymentRepository.findByMpPaymentId(mpPaymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        StatusPayment newStatusEnum;
        try {
            newStatusEnum = StatusPayment.valueOf(mpStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            newStatusEnum = StatusPayment.PENDING; // valor por defecto
        }

        payment.setStatus(newStatusEnum);
        paymentRepository.save(payment);

        PaymentLog log = new PaymentLog();
        log.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        log.setMpOperationId(mpPaymentId);
        log.setStatus(newStatusEnum.name());
        log.setUserEmail(userEmail);
        log.setTimestamp(LocalDateTime.now());
        paymentLogRepository.save(log);
    }

    /**
     * Processes webhook notifications received from Mercado Pago.
     * Retrieves payment details using the Mercado Pago API, updates the corresponding
     * payment status in the database, and logs the notification event.
     * In case of errors, the failure is recorded in the log with an error message.
     *
     * @param mpPaymentId The Mercado Pago payment ID included in the webhook notification.
     */
    public void processWebhookNotification(String mpPaymentId) {
        try {
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));

            // Traer los datos del pago desde la API de MP
            PaymentClient client = new com.mercadopago.client.payment.PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(mpPaymentId));

            String status = mpPayment.getStatus();
            String payerEmail = mpPayment.getPayer() != null ? mpPayment.getPayer().getEmail() : "unknown";

            // Actualizar estado en DB y registrar log
            updatePaymentStatus(mpPaymentId, status, payerEmail);

        } catch (Exception e) {
            PaymentLog log = new PaymentLog();
            log.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            log.setMpOperationId(mpPaymentId);
            log.setStatus("WEBHOOK_ERROR");
            log.setError(e.getMessage());
            log.setTimestamp(LocalDateTime.now());
            paymentLogRepository.save(log);
        }
    }

}


