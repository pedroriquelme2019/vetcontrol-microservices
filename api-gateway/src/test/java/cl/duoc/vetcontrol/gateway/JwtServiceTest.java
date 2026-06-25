package cl.duoc.vetcontrol.gateway;

import cl.duoc.vetcontrol.gateway.dto.JwtUserData;
import cl.duoc.vetcontrol.gateway.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "vetcontrol-secret-key-2026-vetcontrol-secret-key-2026";

    private static final String OTRO_SECRET =
            "otro-secret-key-2026-vetcontrol-otro-secret-key-2026";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        jwtService =
                new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                SECRET
        );
    }

    @Test
    void validarYObtenerDatosDebeRetornarUsuarioValido() {

        String token =
                crearToken(
                        "admin",
                        "ADMIN",
                        10L,
                        fechaFutura(),
                        SECRET
                );

        JwtUserData resultado =
                jwtService.validarYObtenerDatos(
                        token
                );

        assertAll(
                () -> assertEquals(
                        "admin",
                        resultado.getUsername()
                ),
                () -> assertEquals(
                        "ADMIN",
                        resultado.getRole()
                ),
                () -> assertEquals(
                        10L,
                        resultado.getUserId()
                )
        );
    }

    @Test
    void validarYObtenerDatosDebeAceptarUserIdComoTexto() {

        String token =
                crearToken(
                        "recepcion",
                        "RECEPCIONISTA",
                        "25",
                        fechaFutura(),
                        SECRET
                );

        JwtUserData resultado =
                jwtService.validarYObtenerDatos(
                        token
                );

        assertEquals(
                25L,
                resultado.getUserId()
        );
    }

    @Test
    void userIdTextoInvalidoDebeRetornarNulo() {

        String token =
                crearToken(
                        "vet",
                        "VETERINARIO",
                        "no-es-numero",
                        fechaFutura(),
                        SECRET
                );

        JwtUserData resultado =
                jwtService.validarYObtenerDatos(
                        token
                );

        assertNull(
                resultado.getUserId()
        );
    }

    @Test
    void userIdTextoVacioDebeRetornarNulo() {

        String token =
                crearToken(
                        "vet",
                        "VETERINARIO",
                        "",
                        fechaFutura(),
                        SECRET
                );

        JwtUserData resultado =
                jwtService.validarYObtenerDatos(
                        token
                );

        assertNull(
                resultado.getUserId()
        );
    }

    @Test
    void userIdDeOtroTipoDebeRetornarNulo() {

        String token =
                crearToken(
                        "admin",
                        "ADMIN",
                        true,
                        fechaFutura(),
                        SECRET
                );

        JwtUserData resultado =
                jwtService.validarYObtenerDatos(
                        token
                );

        assertNull(
                resultado.getUserId()
        );
    }

    @Test
    void tokenSinUserIdDebeSerValido() {

        String token =
                crearToken(
                        "admin",
                        "ADMIN",
                        null,
                        fechaFutura(),
                        SECRET
                );

        JwtUserData resultado =
                jwtService.validarYObtenerDatos(
                        token
                );

        assertAll(
                () -> assertEquals(
                        "admin",
                        resultado.getUsername()
                ),
                () -> assertEquals(
                        "ADMIN",
                        resultado.getRole()
                ),
                () -> assertNull(
                        resultado.getUserId()
                )
        );
    }

    @Test
    void tokenNuloDebeSerRechazado() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService
                                .validarYObtenerDatos(null)
                );

        assertEquals(
                "El token es obligatorio",
                exception.getMessage()
        );
    }

    @Test
    void tokenVacioDebeSerRechazado() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService
                                .validarYObtenerDatos("   ")
                );

        assertEquals(
                "El token es obligatorio",
                exception.getMessage()
        );
    }

    @Test
    void tokenSinUsuarioDebeSerRechazado() {

        String token =
                crearToken(
                        null,
                        "ADMIN",
                        1L,
                        fechaFutura(),
                        SECRET
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService
                                .validarYObtenerDatos(token)
                );

        assertEquals(
                "El token no contiene un usuario válido",
                exception.getMessage()
        );
    }

    @Test
    void tokenConUsuarioVacioDebeSerRechazado() {

        String token =
                crearToken(
                        "   ",
                        "ADMIN",
                        1L,
                        fechaFutura(),
                        SECRET
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService
                                .validarYObtenerDatos(token)
                );

        assertEquals(
                "El token no contiene un usuario válido",
                exception.getMessage()
        );
    }

    @Test
    void tokenSinRolDebeSerRechazado() {

        String token =
                crearToken(
                        "admin",
                        null,
                        1L,
                        fechaFutura(),
                        SECRET
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService
                                .validarYObtenerDatos(token)
                );

        assertEquals(
                "El token no contiene un rol válido",
                exception.getMessage()
        );
    }

    @Test
    void tokenConRolNoPermitidoDebeSerRechazado() {

        String token =
                crearToken(
                        "usuario",
                        "SUPERADMIN",
                        1L,
                        fechaFutura(),
                        SECRET
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> jwtService
                                .validarYObtenerDatos(token)
                );

        assertEquals(
                "El token no contiene un rol válido",
                exception.getMessage()
        );
    }

    @Test
    void tokenExpiradoDebeSerRechazado() {

        String token =
                crearToken(
                        "admin",
                        "ADMIN",
                        1L,
                        new Date(
                                System.currentTimeMillis()
                                        - 60_000
                        ),
                        SECRET
                );

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService
                        .validarYObtenerDatos(token)
        );
    }

    @Test
    void tokenFirmadoConOtraClaveDebeSerRechazado() {

        String token =
                crearToken(
                        "admin",
                        "ADMIN",
                        1L,
                        fechaFutura(),
                        OTRO_SECRET
                );

        assertThrows(
                JwtException.class,
                () -> jwtService
                        .validarYObtenerDatos(token)
        );
    }

    @Test
    void tieneRolDebeRetornarVerdadero() {

        JwtUserData usuario =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        1L
                );

        assertTrue(
                jwtService.tieneRol(
                        usuario,
                        "ADMIN"
                )
        );
    }

    @Test
    void tieneRolDebeRetornarFalsoCuandoNoCoincide() {

        JwtUserData usuario =
                new JwtUserData(
                        "vet",
                        "VETERINARIO",
                        2L
                );

        assertFalse(
                jwtService.tieneRol(
                        usuario,
                        "ADMIN"
                )
        );
    }

    @Test
    void tieneRolDebeRetornarFalsoConUsuarioNulo() {

        assertFalse(
                jwtService.tieneRol(
                        null,
                        "ADMIN"
                )
        );
    }

    @Test
    void tieneRolDebeRetornarFalsoConRolNulo() {

        JwtUserData usuario =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        1L
                );

        assertFalse(
                jwtService.tieneRol(
                        usuario,
                        null
                )
        );
    }

    private String crearToken(
            String username,
            String role,
            Object userId,
            Date expiration,
            String signingSecret
    ) {

        Key key =
                Keys.hmacShaKeyFor(
                        signingSecret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        var builder =
                Jwts.builder()
                        .setIssuedAt(
                                new Date()
                        )
                        .setExpiration(
                                expiration
                        );

        if (username != null) {
            builder.setSubject(
                    username
            );
        }

        if (role != null) {
            builder.claim(
                    "role",
                    role
            );
        }

        if (userId != null) {
            builder.claim(
                    "userId",
                    userId
            );
        }

        return builder
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    private Date fechaFutura() {

        return new Date(
                System.currentTimeMillis()
                        + 60 * 60 * 1000
        );
    }
}
