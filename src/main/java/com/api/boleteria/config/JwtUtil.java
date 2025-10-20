package com.api.boleteria.config;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Clase utilitaria para crear y validar tokens JWT.
 */
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION = 3600000L;

    /**
     * Crea un token JWT con el nombre de usuario y roles indicados.
     *
     * @param username Nombre de usuario para asignar al token.
     * @param roles    Lista de roles asociados al usuario.
     * @return Token JWT firmado como String.
     */
    public static String createToken(String username, List<String> roles) {

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Valida que el token JWT sea válido y no haya expirado.
     *
     * @param token Token JWT a validar.
     * @return true si el token es válido; false si es inválido o expirado.
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("JWT expirado: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("JWT inválido: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene el nombre de usuario (subject) del token JWT.
     *
     * @param token Token JWT del cual extraer el nombre de usuario.
     * @return Nombre de usuario contenido en el token.
     */
    public static String getUsername(String token) {

        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Obtiene la lista de roles del token JWT.
     *
     * @param token Token JWT del cual extraer los roles.
     * @return Lista de roles almacenados en el token.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRoles(String token) {
        return (List<String>) Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .get("roles");
    }
}