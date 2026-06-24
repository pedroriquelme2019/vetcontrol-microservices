package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.model.Role;
import cl.duoc.vetcontrol.auth.model.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {

    @Test
    void constructorDebeAsignarDatos() {
        UserAccount usuario = new UserAccount(
                "admin",
                "admin@vetcontrol.cl",
                "hash",
                Role.ADMIN,
                true
        );

        assertAll(
                () -> assertEquals(
                        "admin",
                        usuario.getUsername()
                ),
                () -> assertEquals(
                        "admin@vetcontrol.cl",
                        usuario.getEmail()
                ),
                () -> assertEquals(
                        "hash",
                        usuario.getPasswordHash()
                ),
                () -> assertEquals(
                        Role.ADMIN,
                        usuario.getRole()
                ),
                () -> assertTrue(
                        usuario.isEnabled()
                )
        );
    }

    @Test
    void gettersYSettersDebenFuncionar() {
        UserAccount usuario = new UserAccount();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        6,
                        1,
                        10,
                        0
                );

        usuario.setId(1L);
        usuario.setUsername("vet");
        usuario.setEmail("vet@vetcontrol.cl");
        usuario.setPasswordHash("hash");
        usuario.setRole(Role.VETERINARIO);
        usuario.setEnabled(false);
        usuario.setCreatedAt(fecha);

        assertAll(
                () -> assertEquals(1L, usuario.getId()),
                () -> assertEquals(
                        "vet",
                        usuario.getUsername()
                ),
                () -> assertEquals(
                        "vet@vetcontrol.cl",
                        usuario.getEmail()
                ),
                () -> assertEquals(
                        "hash",
                        usuario.getPasswordHash()
                ),
                () -> assertEquals(
                        Role.VETERINARIO,
                        usuario.getRole()
                ),
                () -> assertFalse(usuario.isEnabled()),
                () -> assertEquals(
                        fecha,
                        usuario.getCreatedAt()
                )
        );
    }

    @Test
    void prePersistDebeAsignarFechaSiEsNula() {
        UserAccount usuario = new UserAccount();

        usuario.setCreatedAt(null);
        usuario.prePersist();

        assertNotNull(usuario.getCreatedAt());
    }

    @Test
    void prePersistNoDebeCambiarFechaExistente() {
        UserAccount usuario = new UserAccount();

        LocalDateTime fecha =
                LocalDateTime.of(
                        2026,
                        5,
                        1,
                        10,
                        0
                );

        usuario.setCreatedAt(fecha);
        usuario.prePersist();

        assertEquals(
                fecha,
                usuario.getCreatedAt()
        );
    }

    @Test
    void passwordHashDebeTenerJsonIgnore()
            throws Exception {

        Field campo =
                UserAccount.class
                        .getDeclaredField(
                                "passwordHash"
                        );

        assertTrue(
                campo.isAnnotationPresent(
                        JsonIgnore.class
                )
        );
    }
}