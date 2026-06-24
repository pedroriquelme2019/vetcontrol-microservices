package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.model.Role;
import cl.duoc.vetcontrol.auth.model.UserAccount;
import cl.duoc.vetcontrol.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "vetcontrol-secret-key-2026-vetcontrol-secret-key-2026";

    private JwtService service;

    @BeforeEach
    void setUp() {
        service = new JwtService();

        ReflectionTestUtils.setField(
                service,
                "secret",
                SECRET
        );

        ReflectionTestUtils.setField(
                service,
                "expirationHours",
                8L
        );
    }

    @Test
    void generateTokenDebeIncluirClaimsCorrectos() {
        UserAccount usuario = new UserAccount(
                "admin",
                "admin@vetcontrol.cl",
                "hash",
                Role.ADMIN,
                true
        );

        usuario.setId(10L);

        String token =
                service.generateToken(usuario);

        Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(
                                Keys.hmacShaKeyFor(
                                        SECRET.getBytes(
                                                StandardCharsets.UTF_8
                                        )
                                )
                        )
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

        assertAll(
                () -> assertEquals(
                        "admin",
                        claims.getSubject()
                ),
                () -> assertEquals(
                        "ADMIN",
                        claims.get(
                                "role",
                                String.class
                        )
                ),
                () -> assertEquals(
                        10,
                        claims.get(
                                "userId",
                                Integer.class
                        )
                ),
                () -> assertNotNull(
                        claims.getIssuedAt()
                ),
                () -> assertNotNull(
                        claims.getExpiration()
                )
        );
    }

    @Test
    void generateTokenDebeExpirarEnOchoHoras() {
        UserAccount usuario = new UserAccount(
                "vet",
                "vet@vetcontrol.cl",
                "hash",
                Role.VETERINARIO,
                true
        );

        usuario.setId(3L);

        String token =
                service.generateToken(usuario);

        Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(
                                Keys.hmacShaKeyFor(
                                        SECRET.getBytes(
                                                StandardCharsets.UTF_8
                                        )
                                )
                        )
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

        Date inicio = claims.getIssuedAt();
        Date expiracion = claims.getExpiration();

        long diferencia =
                expiracion.getTime()
                        - inicio.getTime();

        long ochoHoras =
                8L * 60 * 60 * 1000;

        assertTrue(
                Math.abs(diferencia - ochoHoras)
                        < 2000
        );
    }
}