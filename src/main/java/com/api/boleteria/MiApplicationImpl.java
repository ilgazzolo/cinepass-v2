package com.api.boleteria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// SDK de Mercado Pago
import com.mercadopago.MercadoPagoConfig;

@SpringBootApplication
public class MiApplicationImpl {
	public static void main(String[] args) {

		SpringApplication.run(MiApplicationImpl.class, args);

	}
}
