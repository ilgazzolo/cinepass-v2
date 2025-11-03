package com.api.boleteria.mercadopago.service;

import com.api.boleteria.dto.detail.TicketDetailDTO;
import com.api.boleteria.dto.request.TicketRequestDTO;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.log.PaymentLog;
import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.model.Payment;
import com.api.boleteria.model.Ticket;
import com.api.boleteria.model.User;
import com.api.boleteria.model.enums.StatusPayment;
import com.api.boleteria.repository.IPaymentLogRepository;
import com.api.boleteria.repository.IPaymentRepository;
import com.api.boleteria.repository.ITicketRepository;
import com.api.boleteria.repository.IUserRepository;
import com.api.boleteria.service.TicketService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.MercadoPagoConfig;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final ITicketRepository ticketRepository;
    private final IUserRepository userRepository;
    private final TicketService ticketService;



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
    @Transactional
    public PaymentResponseDTO createPreference(PaymentRequestDTO dto) {
        try {
            // Inicializar SDK de Mercado Pago
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));

            // Crear ítem de la preferencia usando unitPrice del DTO
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

            // Devolver DTO con la URL de sandbox para redirigir al pago
            return new PaymentResponseDTO(
                    preference.getId(),
                    // preference.getInitPoint() para produccion
                    preference.getSandboxInitPoint()
            );

        } catch (MPApiException apiException) {
            System.out.println("Status Code: " + apiException.getStatusCode());
            System.out.println("Error Details: " + apiException.getApiResponse().getContent());
            apiException.printStackTrace();
            throw new RuntimeException("Error generating payment preference.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating payment preference: " + e.getMessage());
        }
    }



    /**
     * Updates (or creates) a payment record in the database based on Mercado Pago webhook data.
     * Ensures the payment status and logs are consistent.
     *
     * @param mpPaymentId The Mercado Pago payment ID.
     * @param mpStatus The payment status from Mercado Pago (e.g., "approved", "pending", "rejected").
     * @param userEmail The payer's email associated with the payment.
     */
    @Transactional
    public Payment updatePaymentStatus(String mpPaymentId, String mpStatus, String userEmail) {
        // Intentar obtener el pago existente
        Payment payment = paymentRepository.findByMpPaymentId(mpPaymentId)
                .orElseGet(() -> {
                    // Si no existe, crear uno nuevo
                    Payment newPayment = new Payment();
                    newPayment.setMpPaymentId(mpPaymentId);
                    newPayment.setUserEmail(userEmail);
                    newPayment.setCreatedAt(LocalDateTime.now());
                    return newPayment;
                });

        // Determinar el nuevo estado
        StatusPayment newStatusEnum;
        try {
            newStatusEnum = StatusPayment.valueOf(mpStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            newStatusEnum = StatusPayment.PENDING;
        }

        // Actualizar estado y metadatos
        payment.setStatus(newStatusEnum);
        payment.setUpdatedAt(LocalDateTime.now());

        // Evitar sobrescribir datos si ya existen
        if (payment.getFunction() == null) {
            System.out.println("Warning: Payment without associated function. It will be expected to be assigned before approval.");
        }
        if (payment.getAmount() == null) {
            payment.setAmount(BigDecimal.ZERO); // Evita NPE en cálculos futuros
        }
        if (payment.getQuantity() == null) {
            payment.setQuantity(0);
        }

        // Guardar cambios
        paymentRepository.save(payment);

        // Registrar log
        PaymentLog log = new PaymentLog();
        log.setMpOperationId(mpPaymentId);
        log.setStatus(newStatusEnum.name());
        log.setUserEmail(userEmail);
        log.setTimestamp(LocalDateTime.now());
        paymentLogRepository.save(log);

        return payment; // Devolver el Payment actualizado
    }



    /**
     * Processes webhook notifications received from Mercado Pago.
     * Retrieves payment details using the Mercado Pago API, updates the corresponding
     * payment status in the database, and logs the notification event.
     * In case of errors, the failure is recorded in the log with an error message.
     *
     * @param mpPaymentId The Mercado Pago payment ID included in the webhook notification.
     */
    @Transactional
    public void processWebhookNotification(String mpPaymentId) {
        try {
            // Inicializar SDK de Mercado Pago
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));

            // Obtener el pago desde la API de Mercado Pago
            PaymentClient paymentClient = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment =
                    paymentClient.get(Long.parseLong(mpPaymentId));

            // Extraer datos necesarios
            String mpStatus = mpPayment.getStatus();
            String userEmail = mpPayment.getPayer().getEmail();

            System.out.println("Payment updated from webhook: " + mpPaymentId + " - " + mpStatus);

            // Actualizar o crear el Payment
            Payment payment = updatePaymentStatus(mpPaymentId, mpStatus, userEmail);

            // Solo crear ticket si el pago fue aprobado
            if (StatusPayment.APPROVED.equals(payment.getStatus())) {


                // Buscar usuario por email en lugar de authenticatedUser
                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new NotFoundException("User with email: " + userEmail + " not found"));


                // Construir DTO para crear el ticket
                TicketRequestDTO ticketDTO = new TicketRequestDTO();
                ticketDTO.setFunctionId(payment.getFunction().getId());
                ticketDTO.setQuantity(payment.getQuantity());
                ticketDTO.setTotalAmount(payment.getAmount());
                ticketDTO.setSeats(payment.getSeats());

                // Crear ticket a partir del pago
                TicketDetailDTO ticketDetail = ticketService.createTicketFromPayment(user, ticketDTO);

                // Mapear a entidad para persistir correctamente
                Ticket ticketEntity = ticketService.mapToEntity(
                        user,
                        payment.getFunction(),
                        ticketDTO
                );

                // Asociar ticket al pago y persistir
                payment.setTicket(ticketEntity);
                ticketRepository.save(ticketEntity);
                paymentRepository.save(payment);

                System.out.println("Ticket created and linked to payment ID: " + mpPaymentId);

            }
        } catch (MPApiException e) {
            System.out.println("Error from Mercado Pago API: " + e.getApiResponse().getContent());
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


