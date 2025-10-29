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

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IPaymentRepository paymentRepository;
    private final IPaymentLogRepository paymentLogRepository;

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
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage());
        }
    }

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

