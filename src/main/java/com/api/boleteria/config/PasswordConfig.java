package com.api.boleteria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase de configuración que provee un bean para encriptar contraseñas.
 */

@Configuration
public class PasswordConfig {

    /**
     * Crea y devuelve un PasswordEncoder que utiliza el algoritmo BCrypt.
     *
     * @return PasswordEncoder configurado con BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
