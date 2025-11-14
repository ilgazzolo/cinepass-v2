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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${miapp.ngrokUrl}")
    private String ngrokUrl;


    //-------------------------------SAVE--------------------------------//


    @Transactional
    public PaymentResponseDTO createPreference(PaymentRequestDTO dto) {
        try {
            // Inicializar SDK de Mercado Pago
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));

            // Usuario autenticado
            User user = userService.findAuthenticatedUser();

            // 1) Crear y guardar Payment local primero (para obtener ID)
            Payment payment = new Payment();
            payment.setUserId(user.getId());
            payment.setUserEmail(user.getEmail());
            payment.setQuantity(dto.getSeats().size());

            payment.setAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getSeats().size())));
            payment.setStatus(StatusPayment.APPROVED);          ///HARDCODEADO
            payment.setSeats(dto.getSeats());
            Function function = functionRepository.findById(dto.getFunctionId())
                    .orElseThrow(() -> new NotFoundException("Function with ID: " + dto.getFunctionId() + " not found"));
            payment.setFunction(function);
            payment.prePersist();
            payment.setTicket(this.crearTicket(user.getUsername(),user.getId(),function,dto.getSeats(), dto.getQuantity(),dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity()))));
            paymentRepository.save(payment); // <-- ya tenemos payment.getId()

            // 2) Armar item
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .quantity(dto.getSeats().size())
                    .currencyId("ARS")
                    .unitPrice(dto.getUnitPrice())
                    .build();

            // 3) Back URLs
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(ngrokUrl + "/api/payments/webhooks/success")
                    .pending(ngrokUrl + "/api/payments/webhooks/pending")
                    .failure(ngrokUrl + "/api/payments/webhooks/failure")
                    .build();

            // 4) Crear preferencia con external_reference = ID del Payment local
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(backUrls)
                    .notificationUrl(ngrokUrl + "/api/payments/webhooks/notification")
                    .autoReturn("approved")
                    .externalReference(payment.getId().toString()) // <--- CLAVE
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 5) Guardar preferenceId en el Payment local
            payment.setPreferenceId(preference.getId());
            paymentRepository.save(payment);

            // 6) Log
            PaymentLog log = new PaymentLog();
            log.setStatus("PREFERENCE_CREATED");
            log.setUserEmail(dto.getUserEmail());
            log.setTimestamp(LocalDateTime.now());
            paymentLogRepository.save(log);

            // 7) Respuesta
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


    @Transactional
    public Payment updatePaymentStatus(String mpPaymentId, String mpStatus, String userEmail) {
        Payment payment = paymentRepository.findByMpPaymentId(mpPaymentId)
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setMpPaymentId(mpPaymentId);
                    p.setUserEmail(userEmail);
                    p.setCreatedAt(LocalDateTime.now());
                    return p;
                });

        // Asegurar que queda seteado el mpPaymentId
        if (payment.getMpPaymentId() == null) {
            payment.setMpPaymentId(mpPaymentId);
        }
        if (payment.getUserEmail() == null && userEmail != null) {
            payment.setUserEmail(userEmail);
        }

        StatusPayment newStatusEnum;
        try {
            newStatusEnum = StatusPayment.valueOf(mpStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            newStatusEnum = StatusPayment.PENDING;
        }

        payment.setStatus(newStatusEnum);
        payment.preUpdate();

        if (payment.getAmount() == null) payment.setAmount(BigDecimal.ZERO);
        if (payment.getQuantity() == null) payment.setQuantity(1);

        paymentRepository.save(payment);

        PaymentLog log = new PaymentLog();
        log.setMpOperationId(mpPaymentId);
        log.setStatus(newStatusEnum.name());
        log.setUserEmail(userEmail);
        log.setTimestamp(LocalDateTime.now());
        paymentLogRepository.save(log);

        return payment;
    }



    @Transactional
    public void processWebhookNotification(String mpPaymentId) {
        try {
            // SDK MP
            MercadoPagoConfig.setAccessToken(System.getenv("MP_ACCESS_TOKEN"));

            // Obtener pago real de MP
            PaymentClient paymentClient = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment =
                    paymentClient.get(Long.parseLong(mpPaymentId));

            String mpStatus = mpPayment.getStatus();
            String userEmail = (mpPayment.getPayer() != null) ? mpPayment.getPayer().getEmail() : null;

            System.out.println("Payment updated from webhook: " + mpPaymentId + " - " + mpStatus);

            // --- Vincular al Payment local usando external_reference ---
            Payment payment;
            String externalRef = mpPayment.getExternalReference();
            if (externalRef != null) {
                Long localPaymentId = Long.valueOf(externalRef);
                payment = paymentRepository.findById(localPaymentId)
                        .orElseGet(() -> updatePaymentStatus(mpPaymentId, mpStatus, userEmail));
            } else {
                payment = updatePaymentStatus(mpPaymentId, mpStatus, userEmail);
            }

            // Asegurar que guardamos mpPaymentId
            if (payment.getMpPaymentId() == null) {
                payment.setMpPaymentId(mpPaymentId);
                paymentRepository.save(payment);
            }

            // Si aprobado → crear ticket (una sola vez)
            if (true) {

                // Buscar usuario por ID o email
                User user = null;
                if (payment.getUserId() != null) {
                    user = userRepository.findById(payment.getUserId()).orElse(null);
                }
                if (user == null && userEmail != null) {
                    user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new NotFoundException("User with email " + userEmail + " not found"));
                }

                if (user != null && payment.getFunction() != null) {
                    Function function = payment.getFunction();


                    // 1) Marcar asientos ocupados (lo dejamos acá para no tocar TicketService)
                    List<String> selectedSeats = payment.getSeats();
                    if (selectedSeats != null && !selectedSeats.isEmpty()) {
                        List<Seat> seatsToUpdate = seatRepository.findByFunctionId(function.getId())
                                .stream()
                                .filter(seat -> selectedSeats.contains("R" + seat.getSeatRowNumber() + "C" + seat.getSeatColumnNumber()))
                                .toList();

                        seatsToUpdate.forEach(seat -> seat.setOccupied(true));
                        seatRepository.saveAll(seatsToUpdate);
                    }

                    // 2) Armar DTO con unitPrice calculado (clave)
                    BigDecimal unitPrice = (payment.getQuantity() != null && payment.getQuantity() > 0)
                            ? payment.getAmount().divide(BigDecimal.valueOf(payment.getQuantity()), 2, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    TicketRequestDTO ticketDTO = new TicketRequestDTO();
                    ticketDTO.setFunctionId(function.getId());
                    ticketDTO.setQuantity(payment.getQuantity());
                    ticketDTO.setUnitPrice(unitPrice);
                    ticketDTO.setTotalAmount(payment.getAmount());
                    ticketDTO.setSeats(payment.getSeats());

                    // 3) IMPORTANTE: NO restar capacidad acá.
                    //    Dejar que lo haga TicketService.createTicketFromPayment(...)


                    // 4) Vincular el ticket creado al Payment sin duplicarlo
                    //    (buscamos el último ticket de ese user+function)
                    Ticket ticketEntity = ticketRepository
                            .findTopByUserIdAndFunctionIdOrderByPurchaseDateTimeDesc(user.getId(), function.getId())
                            .orElse(null);

                    payment.setTicket(ticketEntity);
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




    public Ticket crearTicket(String username, Long userId, Function function, List<String> seats, int quantity, BigDecimal mount){
        if (true) {

            // Buscar usuario por ID o email
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }
            if (user == null) {
                user = userRepository.findByUsername(user.getUsername())
                        .orElseThrow(() -> new NotFoundException("User no encontrado"));
            }

            if (user != null && function != null) {


                System.out.println("ACA SE TIENEN QUE CREAR LOS TICKETS--------------------------------------");
                List<Seat> allSeats = seatRepository.findByFunctionId(function.getId());

                // 🔹 Buscar los asientos que matchean los códigos enviados (R1C1)
                List<Seat> seatsToUpdate = allSeats.stream()
                        .filter(seat -> seats.contains("R" + seat.getSeatRowNumber() + "C" + seat.getSeatColumnNumber()))
                        .toList();

                if (seatsToUpdate.isEmpty()) {
                    throw new NotFoundException("Ninguno de los asientos enviados coincide con los disponibles para la función.");
                }

                // 🔹 Marcar ocupados y guardar
                seatsToUpdate.forEach(seat -> seat.setOccupied(true));
                seatRepository.saveAll(seatsToUpdate);

                // 🔹 Convertir a lista de IDs
                List<String> seatIds = seatsToUpdate.stream()
                        .map(seat -> seat.getId().toString())
                        .toList();

                // 2) Armar DTO con unitPrice calculado (clave)
                BigDecimal unitPrice = ( quantity > 0)
                        ? mount.divide(BigDecimal.valueOf(quantity), 2, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                TicketRequestDTO ticketDTO = new TicketRequestDTO();
                ticketDTO.setFunctionId(function.getId());
                ticketDTO.setQuantity(quantity);
                ticketDTO.setUnitPrice(unitPrice);
                ticketDTO.setTotalAmount(mount);
                ticketDTO.setSeats(seatIds);

                // 3) IMPORTANTE: NO restar capacidad acá.
                //    Dejar que lo haga TicketService.createTicketFromPayment(...)
                Ticket ticket = ticketService.createTicketFromPayment(user, ticketDTO);

                return ticket;
            }

        }
        return null;
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

