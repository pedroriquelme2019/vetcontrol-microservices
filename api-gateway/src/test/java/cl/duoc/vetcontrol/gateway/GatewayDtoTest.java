package cl.duoc.vetcontrol.gateway;

import cl.duoc.vetcontrol.gateway.dto.GatewayErrorResponse;
import cl.duoc.vetcontrol.gateway.dto.JwtUserData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GatewayDtoTest {

    @Test
    void gatewayErrorResponseDebeExponerDatos() {

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        25,
                        20,
                        0
                );

        GatewayErrorResponse response =
                new GatewayErrorResponse(
                        fecha,
                        401,
                        "Unauthorized",
                        "Token inválido",
                        "/api/v1/clientes"
                );

        assertAll(
                () -> assertEquals(
                        fecha,
                        response.timestamp()
                ),
                () -> assertEquals(
                        401,
                        response.status()
                ),
                () -> assertEquals(
                        "Unauthorized",
                        response.error()
                ),
                () -> assertEquals(
                        "Token inválido",
                        response.message()
                ),
                () -> assertEquals(
                        "/api/v1/clientes",
                        response.path()
                )
        );
    }

    @Test
    void jwtUserDataDebeExponerDatos() {

        JwtUserData usuario =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        1L
                );

        assertAll(
                () -> assertEquals(
                        "admin",
                        usuario.getUsername()
                ),
                () -> assertEquals(
                        "ADMIN",
                        usuario.getRole()
                ),
                () -> assertEquals(
                        1L,
                        usuario.getUserId()
                )
        );
    }

    @Test
    void objetosConMismosDatosDebenSerIguales() {

        JwtUserData primero =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        1L
                );

        JwtUserData segundo =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        1L
                );

        assertEquals(
                primero,
                segundo
        );

        assertEquals(
                primero.hashCode(),
                segundo.hashCode()
        );

        assertTrue(
                primero.toString()
                        .contains("admin")
        );
    }

    @Test
    void mismoObjetoDebeSerIgualASiMismo() {

        JwtUserData usuario =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        1L
                );

        assertEquals(
                usuario,
                usuario
        );
    }

    @Test
    void objetosDiferentesNoDebenSerIguales() {

        JwtUserData primero =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        1L
                );

        JwtUserData segundo =
                new JwtUserData(
                        "vet",
                        "VETERINARIO",
                        2L
                );

        assertNotEquals(
                primero,
                segundo
        );

        assertNotEquals(
                primero,
                null
        );

        assertNotEquals(
                primero,
                "texto"
        );
    }
}

