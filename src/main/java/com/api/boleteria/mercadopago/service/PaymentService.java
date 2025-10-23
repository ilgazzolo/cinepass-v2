package com.api.boleteria.mercadopago.service;

import com.api.boleteria.log.PaymentLog;
import com.api.boleteria.mercadopago.dto.PaymentRequestDTO;
import com.api.boleteria.mercadopago.dto.PaymentResponseDTO;
import com.api.boleteria.repository.IPaymentLogRepository;
import com.api.boleteria.repository.IPaymentRepository;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
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
            // Setea tu Access Token (usá variable de entorno)
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

            //  Configurar URLs de retorno
            /*PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://tusitio.com/pago-exitoso")
                    .pending("https://tusitio.com/pago-pendiente")
                    .failure("https://tusitio.com/pago-fallido")
                    .build();*/
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    //usando ngrok para que mercado pago pueda acceder a las rutas
                    .success("https://tunnel.ngrok.io/success")
                    .pending("https://tunnel.ngrok.io/pending")
                    .failure("https://tunnel.ngrok.io/failure")
                    .build();

            // Crear preferencia con back_urls y auto_return
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .build();


            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);


            // Registrar log del intento
            PaymentLog log = new PaymentLog();
            log.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            log.setStatus("PREFERENCE_CREATED");
            log.setUserEmail(dto.getUserEmail());
            log.setTimestamp(LocalDateTime.now());
            paymentLogRepository.save(log);

            return new PaymentResponseDTO(
                    preference.getId(),        //  ID único de la preferencia
            //        preference.getInitPoint()  //  devuelve URL para redirigir al pago
                    preference.getSandboxInitPoint()  // para pruebas
            );


        } catch (Exception e) {
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage());
        }
    }
}

