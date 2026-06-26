package cl.duoc.vetcontrol.gateway.service;

import cl.duoc.vetcontrol.gateway.dto.JwtUserData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
public class JwtService {

    private static final Set<String> ROLES_PERMITIDOS =
            Set.of(
                    "ADMIN",
                    "RECEPCIONISTA",
                    "VETERINARIO"
            );

    @Value("${security.jwt.secret}")
    private String secret;

    public JwtUserData validarYObtenerDatos(
            String token
    ) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "El token es obligatorio"
            );
        }

        Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(
                                Keys.hmacShaKeyFor(
                                        secret.getBytes(
                                                StandardCharsets.UTF_8
                                        )
                                )
                        )
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

        String username =
                claims.getSubject();

        String role =
                claims.get(
                        "role",
                        String.class
                );

        if (username == null
                || username.isBlank()) {

            throw new IllegalArgumentException(
                    "El token no contiene un usuario válido"
            );
        }

        if (role == null
                || !ROLES_PERMITIDOS.contains(role)) {

            throw new IllegalArgumentException(
                    "El token no contiene un rol válido"
            );
        }

        Long userId =
                convertirUserId(
                        claims.get("userId")
                );

        return new JwtUserData(
                username,
                role,
                userId
        );
    }

    public boolean tieneRol(
            JwtUserData usuario,
            String roleEsperado
    ) {
        if (usuario == null
                || roleEsperado == null) {

            return false;
        }

        return roleEsperado.equals(
                usuario.getRole()
        );
    }

    private Long convertirUserId(
            Object valor
    ) {
        if (valor instanceof Number numero) {
            return numero.longValue();
        }

        if (valor instanceof String texto
                && !texto.isBlank()) {

            try {
                return Long.valueOf(texto);

            } catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }
}