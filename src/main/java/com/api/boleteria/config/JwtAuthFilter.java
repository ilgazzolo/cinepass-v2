package com.api.boleteria.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro de seguridad que intercepta cada solicitud HTTP para validar el token JWT enviado en el encabezado Authorization.
 *
 * Este filtro verifica la validez y expiración del token JWT. Si es válido, extrae el nombre de usuario y los roles,
 * y establece la autenticación en el contexto de seguridad de Spring Security para controlar el acceso a recursos protegidos.
 *
 * En caso de token inválido o expirado, responde con un estado 401 (No autorizado) y un mensaje descriptivo.
 *
 * Este filtro se ejecuta una única vez por cada solicitud gracias a la extensión de OncePerRequestFilter.
 */

public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * Filtra cada solicitud HTTP para validar el token JWT en el encabezado Authorization.
     * Si el token es válido, establece la autenticación en el contexto de seguridad.
     * En caso de token inválido o expirado, responde con estado 401.
     *
     * @param request  Solicitud HTTP entrante.
     * @param response Respuesta HTTP que puede modificarse en caso de error.
     * @param chain    Cadena de filtros para continuar el procesamiento.
     * @throws ServletException si ocurre un error relacionado con el servlet.
     * @throws IOException      si ocurre un error de entrada/salida.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {

            if (JwtUtil.validateToken(token)) {

                String username = JwtUtil.getUsername(token);
                List<String> roles = JwtUtil.getRoles(token);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido o expirado");
            return;
        }

        chain.doFilter(request, response);
    }
}