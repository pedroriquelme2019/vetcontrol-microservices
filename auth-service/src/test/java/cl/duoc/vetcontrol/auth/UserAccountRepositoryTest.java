package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.model.Role;
import cl.duoc.vetcontrol.auth.model.UserAccount;
import cl.duoc.vetcontrol.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class UserAccountRepositoryTest {

    @Autowired
    private UserAccountRepository repository;

    @Test
    void saveDebeGuardarUsuarioCompleto() {
        UserAccount guardado =
                repository.saveAndFlush(
                        crearUsuario(
                                "admin",
                                "admin@vetcontrol.cl",
                                Role.ADMIN
                        )
                );

        assertAll(
                () -> assertNotNull(guardado.getId()),
                () -> assertNotNull(
                        guardado.getCreatedAt()
                ),
                () -> assertEquals(
                        Role.ADMIN,
                        guardado.getRole()
                ),
                () -> assertTrue(
                        guardado.isEnabled()
                )
        );
    }

    @Test
    void findByUsernameIgnoreCaseDebeIgnorarMayusculas() {
        repository.saveAndFlush(
                crearUsuario(
                        "administrador",
                        "admin@vetcontrol.cl",
                        Role.ADMIN
                )
        );

        Optional<UserAccount> resultado =
                repository.findByUsernameIgnoreCase(
                        "ADMINISTRADOR"
                );

        assertTrue(resultado.isPresent());
    }

    @Test
    void findByEmailIgnoreCaseDebeIgnorarMayusculas() {
        repository.saveAndFlush(
                crearUsuario(
                        "admin",
                        "admin@vetcontrol.cl",
                        Role.ADMIN
                )
        );

        Optional<UserAccount> resultado =
                repository.findByEmailIgnoreCase(
                        "ADMIN@VETCONTROL.CL"
                );

        assertTrue(resultado.isPresent());
    }

    @Test
    void findPorUsernameOCorreoDebeEncontrarPorUsername() {
        repository.saveAndFlush(
                crearUsuario(
                        "recepcion",
                        "recepcion@vetcontrol.cl",
                        Role.RECEPCIONISTA
                )
        );

        Optional<UserAccount> resultado =
                repository
                        .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                                "RECEPCION",
                                "RECEPCION"
                        );

        assertTrue(resultado.isPresent());
        assertEquals(
                Role.RECEPCIONISTA,
                resultado.get().getRole()
        );
    }

    @Test
    void findPorUsernameOCorreoDebeEncontrarPorCorreo() {
        repository.saveAndFlush(
                crearUsuario(
                        "vet",
                        "vet@vetcontrol.cl",
                        Role.VETERINARIO
                )
        );

        Optional<UserAccount> resultado =
                repository
                        .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                                "vet@vetcontrol.cl",
                                "vet@vetcontrol.cl"
                        );

        assertTrue(resultado.isPresent());
        assertEquals("vet", resultado.get().getUsername());
    }

    @Test
    void existsByUsernameDebeDetectarUsuario() {
        repository.saveAndFlush(
                crearUsuario(
                        "admin",
                        "admin@vetcontrol.cl",
                        Role.ADMIN
                )
        );

        assertTrue(
                repository.existsByUsernameIgnoreCase(
                        "ADMIN"
                )
        );

        assertFalse(
                repository.existsByUsernameIgnoreCase(
                        "otro"
                )
        );
    }

    @Test
    void existsByEmailDebeDetectarCorreo() {
        repository.saveAndFlush(
                crearUsuario(
                        "admin",
                        "admin@vetcontrol.cl",
                        Role.ADMIN
                )
        );

        assertTrue(
                repository.existsByEmailIgnoreCase(
                        "ADMIN@VETCONTROL.CL"
                )
        );
    }

    @Test
    void findAllDebeOrdenarPorUsername() {
        repository.saveAndFlush(
                crearUsuario(
                        "vet",
                        "vet@vetcontrol.cl",
                        Role.VETERINARIO
                )
        );

        repository.saveAndFlush(
                crearUsuario(
                        "admin",
                        "admin@vetcontrol.cl",
                        Role.ADMIN
                )
        );

        repository.saveAndFlush(
                crearUsuario(
                        "recepcion",
                        "recepcion@vetcontrol.cl",
                        Role.RECEPCIONISTA
                )
        );

        List<UserAccount> resultado =
                repository
                        .findAllByOrderByUsernameAsc();

        assertEquals(3, resultado.size());
        assertEquals(
                "admin",
                resultado.get(0).getUsername()
        );
        assertEquals(
                "recepcion",
                resultado.get(1).getUsername()
        );
        assertEquals(
                "vet",
                resultado.get(2).getUsername()
        );
    }

    private UserAccount crearUsuario(
            String username,
            String email,
            Role role
    ) {
        return new UserAccount(
                username,
                email,
                "password-hash",
                role,
                true
        );
    }
}