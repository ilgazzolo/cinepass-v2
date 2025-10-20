package com.api.boleteria.mercadopago.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mercadopago")
public class MercadoPagoConfig {
    private String accessToken;
    private String publicKey;
}

//  Y en application.yml:
/*mercadopago:
access-token: ${MP_ACCESS_TOKEN}
public-key: ${MP_PUBLIC_KEY}*/


