package com.api.boleteria.mercadopago.service;

import com.api.boleteria.dto.detail.TicketDetailDTO;
import com.api.boleteria.dto.request.TicketRequestDTO;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.log.PaymentLog;
import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.model.*;
import com.api.boleteria.model.enums.StatusPayment;
import com.api.boleteria.repository.*;
import com.api.boleteria.service.TicketService;
import com.api.boleteria.service.UserService;
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
 * Service class responsible for handling all payment-related operations and
 * communication with the Mercado Pago API. It manages payment preference creation,
 * payment status synchronization through webhooks, and ticket generation upon
 * successful payments. It also keeps a detailed log of all payment events for
 * auditing and debugging purposes.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IPaymentRepository paymentRepository;
    private final IPaymentLogRepository paymentLogRepository;
    private final ITicketRepository ticketRepository;
    private final IFunctionRepository functionRepository;
    private final ISeatRepository seatRepository;
    private final IUserRepository userRepository;
    private final TicketService ticketService;
    private final UserService userService;


    //-------------------------------SAVE--------------------------------//


    /**
     * Creates a new payment preference in Mercado Pago using the provided payment data.
     * The method builds the preference request, sets the item details, back URLs,
     * notification URL, and auto-return configuration. It then sends the request to
     * Mercado Pago to obtain a payment preference and stores the local record in the database.
     *
     * @param dto The {@link PaymentRequestDTO} containing information such as title,
     *            description, quantity, price, function ID, and selected seats.
     * @return A {@link PaymentResponseDTO} with the generated preference ID and the sandbox
     *         URL to redirect the user for payment.
     * @throws RuntimeException if any error occurs during preference creation or API communication.
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
                    .notificationUrl("https://larhonda-progravid-caressively.ngrok-free.dev/api/payments/webhooks/notification")
                    .autoReturn("approved")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            User user = userService.findAuthenticatedUser();    //devuelve el usuario autenticado

            // Crear el registro de pago local
            Payment payment = new Payment();
            payment.setPreferenceId(preference.getId());
            payment.setUserId(user.getId());
            payment.setUserEmail(user.getEmail());
            payment.setQuantity(dto.getQuantity());
            payment.setAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
            payment.setStatus(StatusPayment.PENDING); // estado inicial
            payment.setSeats(dto.getSeats());
            Function function = functionRepository.findById(dto.getFunctionId())
                    .orElseThrow(() -> new NotFoundException("Function with ID: " + dto.getFunctionId() + " not found"));
            payment.setFunction(function);
            payment.prePersist();

            // Registrar creacion del pago
            paymentRepository.save(payment);

            // Registrar log del intento
            PaymentLog log = new PaymentLog();
            log.setStatus("PREFERENCE_CREATED");
            log.setUserEmail(dto.getUserEmail());
            log.setTimestamp(LocalDateTime.now());
            paymentLogRepository.save(log);

            // Devolver DTO con la URL de sandbox para redirigir al pago
            return mapToResponse(preference);

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


    //-------------------------------UPDATE--------------------------------//


    /**
     * Updates or creates a {@link Payment} record in the database based on data received
     * from Mercado Pago. It ensures that the payment status, user information, and logs
     * remain consistent with the latest update from the platform.
     *
     * @param mpPaymentId The Mercado Pago payment ID.
     * @param mpStatus    The payment status received from Mercado Pago (e.g. "approved", "pending", "rejected").
     * @param userEmail   The payer’s email address associated with the payment.
     * @return The updated or newly created {@link Payment} reflecting the current status.
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

        if (payment.getUserEmail() == null && userEmail != null)
            payment.setUserEmail(userEmail);

        // Determinar el nuevo estado
        StatusPayment newStatusEnum;
        try {
            newStatusEnum = StatusPayment.valueOf(mpStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            newStatusEnum = StatusPayment.PENDING;
        }

        // Actualizar estado y metadatos
        payment.setStatus(newStatusEnum);
        payment.preUpdate();

        // Evitar datos nulos
        if (payment.getFunction() == null) {
            System.out.println("Warning: Payment without associated function. It will be expected to be assigned before approval.");
        }
        if (payment.getAmount() == null) {
            payment.setAmount(BigDecimal.ZERO); // Evita NPE en cálculos futuros
        }
        if (payment.getQuantity() == null) {
            payment.setQuantity(1);
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
     * Processes incoming webhook notifications from Mercado Pago.
     * This method retrieves payment details from the Mercado Pago API, updates the
     * local payment record, and logs the notification event. If the payment is approved,
     * it also performs the following actions:
     * <ul>
     *   <li>Updates seat availability for the related function</li>
     *   <li>Decreases the function’s available capacity</li>
     *   <li>Creates and links a new ticket to the payment</li>
     * </ul>
     * Any errors are logged for debugging and consistency tracking.
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
                User user = null;

                if (payment.getUserId() != null) {
                    user = userRepository.findById(payment.getUserId())
                            .orElse(null);
                }
                // Si no lo encuentra por ID, intentar por email
                if (user == null && userEmail != null) {
                    user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new NotFoundException("User with email " + userEmail + " not found"));
                }

                if (user != null && payment.getFunction() != null) {
                    Function function = payment.getFunction();

                    // Actualizar asientos ocupados
                    List<String> selectedSeats = payment.getSeats();
                    if (selectedSeats != null && !selectedSeats.isEmpty()) {
                        List<Seat> seatsToUpdate = seatRepository.findByFunctionId(function.getId())
                                .stream()
                                .filter(seat -> selectedSeats.contains(
                                        "R" + seat.getSeatRowNumber() + "C" + seat.getSeatColumnNumber()
                                ))
                                .toList();

                        seatsToUpdate.forEach(seat -> seat.setOccupied(true));
                        seatRepository.saveAll(seatsToUpdate);
                    }

                    // Actualizar capacidad disponible de la funcion
                    Integer newCapacity = Math.max(0, function.getAvailableCapacity() - payment.getQuantity());
                    function.setAvailableCapacity(newCapacity);
                    functionRepository.save(function);

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


    //-------------------------------MAPS--------------------------------//


    /**
     * Maps a Mercado Pago {@link Preference} object to a {@link PaymentResponseDTO}.
     * This method extracts the preference ID and sandbox initialization URL
     * (used for testing environments) to build the response object.
     *
     * @param preference The Mercado Pago {@link Preference} generated after creating a payment preference.
     * @return A {@link PaymentResponseDTO} containing the preference ID and the sandbox payment URL.
     */
    public PaymentResponseDTO mapToResponse(Preference preference) {
        return new PaymentResponseDTO(
                preference.getId(),
                preference.getSandboxInitPoint() // preference.getInitPoint() para produccion
        );
    }


}

