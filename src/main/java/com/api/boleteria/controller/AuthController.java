package com.api.boleteria.controller;

import com.api.boleteria.dto.request.LoginRequestDTO;
import com.api.boleteria.dto.request.RegisterRequestDTO;
import com.api.boleteria.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para autenticación y registro de usuarios.
 *
 * Permite a los usuarios autenticarse (login) y registrarse en el sistema.
 */
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;

    /**
     * Autentica a un usuario con las credenciales proporcionadas.
     *
     * @param entity DTO con username y password para autenticación.
     * @return ResponseEntity con un mapa que contiene el token JWT u otra información de sesión.
     */

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO entity) {
        return ResponseEntity.ok(userService.login(entity, authManager));
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param entity DTO con los datos para registrar al usuario.
     * @return ResponseEntity con mensaje de éxito o conflicto si el username ya existe.
     */

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDTO entity) {
        userService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito");
    }

}
