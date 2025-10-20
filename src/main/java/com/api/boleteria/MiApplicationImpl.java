package com.api.boleteria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// SDK de Mercado Pago
import com.mercadopago.MercadoPagoConfig;

@SpringBootApplication
public class MiApplicationImpl {

	public static void main(String[] args) {

		// Agrega credenciales
		MercadoPagoConfig.setAccessToken("APP_USR-4807160800633564-102010-fc4fafee48a355e8bd9f78df4f16a9b6-2936834368");

		SpringApplication.run(MiApplicationImpl.class, args);

	}
}
