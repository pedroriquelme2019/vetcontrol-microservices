package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.config.DataSeeder;
import cl.duoc.vetcontrol.auth.model.Role;
import cl.duoc.vetcontrol.auth.model.UserAccount;
import cl.duoc.vetcontrol.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataSeederTest {

    private UserAccountRepository repository;
    private PasswordEncoder encoder;
    private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        repository =
                mock(UserAccountRepository.class);

        encoder =
                mock(PasswordEncoder.class);

        seeder = new DataSeeder();

        ReflectionTestUtils.setField(
                seeder,
                "seedEnabled",
                true
        );

        ReflectionTestUtils.setField(
                seeder,
                "adminPassword",
                "admin123"
        );

        ReflectionTestUtils.setField(
                seeder,
                "recepcionPassword",
                "recepcion123"
        );

        ReflectionTestUtils.setField(
                seeder,
                "veterinarioPassword",
                "vet123"
        );
    }

    @Test
    void seedDeshabilitadoNoDebeCrearUsuarios()
            throws Exception {

        ReflectionTestUtils.setField(
                seeder,
                "seedEnabled",
                false
        );

        CommandLineRunner runner =
                seeder.seedUsers(
                        repository,
                        encoder
                );

        runner.run();

        verifyNoInteractions(
                repository,
                encoder
        );
    }

    @Test
    void debeCrearTresUsuariosIniciales()
            throws Exception {

        when(repository.existsByUsernameIgnoreCase(
                anyString()
        )).thenReturn(false);

        when(encoder.encode(anyString()))
                .thenAnswer(invocation ->
                        "hash-"
                                + invocation.getArgument(0)
                );

        CommandLineRunner runner =
                seeder.seedUsers(
                        repository,
                        encoder
                );

        runner.run();

        ArgumentCaptor<UserAccount> captor =
                ArgumentCaptor.forClass(
                        UserAccount.class
                );

        verify(repository, times(3))
                .save(captor.capture());

        List<UserAccount> usuarios =
                captor.getAllValues();

        assertEquals(3, usuarios.size());

        assertAll(
                () -> assertEquals(
                        Role.ADMIN,
                        usuarios.get(0).getRole()
                ),
                () -> assertEquals(
                        Role.RECEPCIONISTA,
                        usuarios.get(1).getRole()
                ),
                () -> assertEquals(
                        Role.VETERINARIO,
                        usuarios.get(2).getRole()
                ),
                () -> assertTrue(
                        usuarios.stream()
                                .allMatch(
                                        UserAccount::isEnabled
                                )
                )
        );
    }

    @Test
    void usuariosExistentesNoDebenCrearse()
            throws Exception {

        when(repository.existsByUsernameIgnoreCase(
                anyString()
        )).thenReturn(true);

        CommandLineRunner runner =
                seeder.seedUsers(
                        repository,
                        encoder
                );

        runner.run();

        verify(repository, never())
                .save(any());

        verifyNoInteractions(encoder);
    }
}