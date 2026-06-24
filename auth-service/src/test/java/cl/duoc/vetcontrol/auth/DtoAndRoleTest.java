package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.dto.AuthResponse;
import cl.duoc.vetcontrol.auth.dto.UserResponse;
import cl.duoc.vetcontrol.auth.model.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndRoleTest {

    @Test
    void authResponseDebeExponerDatos() {
        AuthResponse response =
                new AuthResponse(
                        "token",
                        "Bearer",
                        "admin",
                        "ADMIN"
                );

        assertAll(
                () -> assertEquals(
                        "token",
                        response.token()
                ),
                () -> assertEquals(
                        "Bearer",
                        response.tokenType()
                ),
                () -> assertEquals(
                        "admin",
                        response.username()
                ),
                () -> assertEquals(
                        "ADMIN",
                        response.role()
                )
        );
    }

    @Test
    void userResponseDebeExponerDatos() {
        LocalDateTime fecha =
                LocalDateTime.now();

        UserResponse response =
                new UserResponse(
                        1L,
                        "admin",
                        "admin@vetcontrol.cl",
                        Role.ADMIN,
                        true,
                        fecha
                );

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals(
                        "admin",
                        response.username()
                ),
                () -> assertEquals(
                        "admin@vetcontrol.cl",
                        response.email()
                ),
                () -> assertEquals(
                        Role.ADMIN,
                        response.role()
                ),
                () -> assertTrue(response.enabled()),
                () -> assertEquals(
                        fecha,
                        response.createdAt()
                )
        );
    }

    @Test
    void roleDebeContenerTodosLosRoles() {
        assertArrayEquals(
                new Role[]{
                        Role.ADMIN,
                        Role.RECEPCIONISTA,
                        Role.VETERINARIO
                },
                Role.values()
        );
    }

    @Test
    void valueOfDebeRecuperarRol() {
        assertEquals(
                Role.VETERINARIO,
                Role.valueOf("VETERINARIO")
        );
    }
}