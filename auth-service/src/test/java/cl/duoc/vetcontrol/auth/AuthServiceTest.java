package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.dto.AuthResponse;
import cl.duoc.vetcontrol.auth.dto.LoginRequest;
import cl.duoc.vetcontrol.auth.dto.UserRequest;
import cl.duoc.vetcontrol.auth.dto.UserResponse;
import cl.duoc.vetcontrol.auth.dto.UserUpdateRequest;
import cl.duoc.vetcontrol.auth.exception.BusinessException;
import cl.duoc.vetcontrol.auth.exception.InvalidCredentialsException;
import cl.duoc.vetcontrol.auth.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.auth.model.Role;
import cl.duoc.vetcontrol.auth.model.UserAccount;
import cl.duoc.vetcontrol.auth.repository.UserAccountRepository;
import cl.duoc.vetcontrol.auth.service.AuthService;
import cl.duoc.vetcontrol.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserAccountRepository repository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserAccountRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);

        service = new AuthService(
                repository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void loginDebeRetornarTokenConCredencialesCorrectas() {
        UserAccount usuario = crearUsuario();

        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                "admin",
                "admin"
        )).thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "admin123",
                usuario.getPasswordHash()
        )).thenReturn(true);

        when(jwtService.generateToken(usuario))
                .thenReturn("jwt-token");

        AuthResponse resultado = service.login(
                new LoginRequest(
                        "  admin  ",
                        "admin123"
                )
        );

        assertAll(
                () -> assertEquals("jwt-token", resultado.token()),
                () -> assertEquals("Bearer", resultado.tokenType()),
                () -> assertEquals("admin", resultado.username()),
                () -> assertEquals("ADMIN", resultado.role())
        );

        verify(jwtService).generateToken(usuario);
    }

    @Test
    void loginDebeAceptarCorreoElectronico() {
        UserAccount usuario = crearUsuario();

        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                "admin@vetcontrol.cl",
                "admin@vetcontrol.cl"
        )).thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "admin123",
                usuario.getPasswordHash()
        )).thenReturn(true);

        when(jwtService.generateToken(usuario))
                .thenReturn("token-correo");

        AuthResponse resultado = service.login(
                new LoginRequest(
                        "admin@vetcontrol.cl",
                        "admin123"
                )
        );

        assertEquals(
                "token-correo",
                resultado.token()
        );
    }

    @Test
    void loginDebeRechazarUsuarioInexistente() {
        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                "desconocido",
                "desconocido"
        )).thenReturn(Optional.empty());

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> service.login(
                                new LoginRequest(
                                        "desconocido",
                                        "clave123"
                                )
                        )
                );

        assertEquals(
                "Credenciales inválidas",
                exception.getMessage()
        );

        verifyNoInteractions(
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void loginDebeRechazarUsuarioDeshabilitado() {
        UserAccount usuario = crearUsuario();
        usuario.setEnabled(false);

        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                "admin",
                "admin"
        )).thenReturn(Optional.of(usuario));

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> service.login(
                                new LoginRequest(
                                        "admin",
                                        "admin123"
                                )
                        )
                );

        assertEquals(
                "Credenciales inválidas",
                exception.getMessage()
        );

        verifyNoInteractions(
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void loginDebeRechazarPasswordIncorrecta() {
        UserAccount usuario = crearUsuario();

        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                "admin",
                "admin"
        )).thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "incorrecta",
                usuario.getPasswordHash()
        )).thenReturn(false);

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> service.login(
                                new LoginRequest(
                                        "admin",
                                        "incorrecta"
                                )
                        )
                );

        assertEquals(
                "Credenciales inválidas",
                exception.getMessage()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void findAllDebeRetornarUsuariosOrdenadosSinPassword() {
        when(repository.findAllByOrderByUsernameAsc())
                .thenReturn(List.of(crearUsuario()));

        List<UserResponse> resultado =
                service.findAll();

        assertEquals(1, resultado.size());

        UserResponse usuario = resultado.get(0);

        assertAll(
                () -> assertEquals(1L, usuario.id()),
                () -> assertEquals("admin", usuario.username()),
                () -> assertEquals(
                        "admin@vetcontrol.cl",
                        usuario.email()
                ),
                () -> assertEquals(Role.ADMIN, usuario.role()),
                () -> assertTrue(usuario.enabled())
        );

        verify(repository)
                .findAllByOrderByUsernameAsc();
    }

    @Test
    void findByIdDebeRetornarUsuario() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(crearUsuario()));

        UserResponse resultado =
                service.findById(1L);

        assertEquals(1L, resultado.id());
        assertEquals("admin", resultado.username());
    }

    @Test
    void findByIdDebeFallarCuandoNoExiste() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findById(99L)
                );

        assertEquals(
                "Usuario no encontrado: 99",
                exception.getMessage()
        );
    }

    @Test
    void createDebeGuardarUsuarioNormalizado() {
        UserRequest request = new UserRequest(
                "  Nuevo.Usuario  ",
                "  NUEVO@VETCONTROL.CL  ",
                "password123",
                Role.VETERINARIO
        );

        when(repository.existsByUsernameIgnoreCase(
                "nuevo.usuario"
        )).thenReturn(false);

        when(repository.existsByEmailIgnoreCase(
                "nuevo@vetcontrol.cl"
        )).thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("password-hash");

        when(repository.save(any(UserAccount.class)))
                .thenAnswer(invocation -> {
                    UserAccount usuario =
                            invocation.getArgument(0);

                    usuario.setId(5L);
                    usuario.setCreatedAt(
                            LocalDateTime.now()
                    );

                    return usuario;
                });

        UserResponse resultado =
                service.create(request);

        assertAll(
                () -> assertEquals(5L, resultado.id()),
                () -> assertEquals(
                        "nuevo.usuario",
                        resultado.username()
                ),
                () -> assertEquals(
                        "nuevo@vetcontrol.cl",
                        resultado.email()
                ),
                () -> assertEquals(
                        Role.VETERINARIO,
                        resultado.role()
                ),
                () -> assertTrue(resultado.enabled())
        );

        ArgumentCaptor<UserAccount> captor =
                ArgumentCaptor.forClass(
                        UserAccount.class
                );

        verify(repository).save(captor.capture());

        UserAccount guardado = captor.getValue();

        assertEquals(
                "password-hash",
                guardado.getPasswordHash()
        );
    }

    @Test
    void createDebeRechazarUsernameDuplicado() {
        when(repository.existsByUsernameIgnoreCase(
                "admin"
        )).thenReturn(true);

        UserRequest request = new UserRequest(
                "admin",
                "otro@vetcontrol.cl",
                "password123",
                Role.ADMIN
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "El username ya existe",
                exception.getMessage()
        );

        verify(repository, never())
                .existsByEmailIgnoreCase(anyString());

        verify(repository, never())
                .save(any());
    }

    @Test
    void createDebeRechazarCorreoDuplicado() {
        when(repository.existsByUsernameIgnoreCase(
                "nuevo"
        )).thenReturn(false);

        when(repository.existsByEmailIgnoreCase(
                "admin@vetcontrol.cl"
        )).thenReturn(true);

        UserRequest request = new UserRequest(
                "nuevo",
                "admin@vetcontrol.cl",
                "password123",
                Role.ADMIN
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(request)
                );

        assertEquals(
                "El correo ya existe",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void updateDebeModificarTodosLosCampos() {
        UserAccount usuario = crearUsuario();

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.findByEmailIgnoreCase(
                "nuevo@vetcontrol.cl"
        )).thenReturn(Optional.empty());

        when(passwordEncoder.encode("nuevaClave123"))
                .thenReturn("nuevo-hash");

        when(repository.save(usuario))
                .thenReturn(usuario);

        UserUpdateRequest request =
                new UserUpdateRequest(
                        "  NUEVO@VETCONTROL.CL ",
                        "nuevaClave123",
                        Role.RECEPCIONISTA,
                        false
                );

        UserResponse resultado =
                service.update(1L, request);

        assertAll(
                () -> assertEquals(
                        "nuevo@vetcontrol.cl",
                        resultado.email()
                ),
                () -> assertEquals(
                        Role.RECEPCIONISTA,
                        resultado.role()
                ),
                () -> assertFalse(resultado.enabled()),
                () -> assertEquals(
                        "nuevo-hash",
                        usuario.getPasswordHash()
                )
        );

        verify(repository).save(usuario);
    }

    @Test
    void updateDebePermitirMantenerElMismoCorreo() {
        UserAccount usuario = crearUsuario();

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.findByEmailIgnoreCase(
                "admin@vetcontrol.cl"
        )).thenReturn(Optional.of(usuario));

        when(repository.save(usuario))
                .thenReturn(usuario);

        UserResponse resultado =
                service.update(
                        1L,
                        new UserUpdateRequest(
                                "admin@vetcontrol.cl",
                                null,
                                null,
                                null
                        )
                );

        assertEquals(
                "admin@vetcontrol.cl",
                resultado.email()
        );
    }

    @Test
    void updateDebeRechazarCorreoDeOtroUsuario() {
        UserAccount usuario = crearUsuario();

        UserAccount otro = new UserAccount();
        otro.setId(2L);
        otro.setEmail("ocupado@vetcontrol.cl");

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.findByEmailIgnoreCase(
                "ocupado@vetcontrol.cl"
        )).thenReturn(Optional.of(otro));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(
                                1L,
                                new UserUpdateRequest(
                                        "ocupado@vetcontrol.cl",
                                        null,
                                        null,
                                        null
                                )
                        )
                );

        assertEquals(
                "El correo ya existe",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void updateConCamposNulosDebeMantenerDatos() {
        UserAccount usuario = crearUsuario();

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.save(usuario))
                .thenReturn(usuario);

        UserResponse resultado =
                service.update(
                        1L,
                        new UserUpdateRequest(
                                null,
                                null,
                                null,
                                null
                        )
                );

        assertAll(
                () -> assertEquals(
                        "admin@vetcontrol.cl",
                        resultado.email()
                ),
                () -> assertEquals(
                        Role.ADMIN,
                        resultado.role()
                ),
                () -> assertTrue(resultado.enabled())
        );

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void cambiarEstadoDebeDeshabilitarUsuario() {
        UserAccount usuario = crearUsuario();

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.save(usuario))
                .thenReturn(usuario);

        UserResponse resultado =
                service.cambiarEstado(
                        1L,
                        false
                );

        assertFalse(resultado.enabled());

        verify(repository).save(usuario);
    }

    @Test
    void cambiarEstadoDebeHabilitarUsuario() {
        UserAccount usuario = crearUsuario();
        usuario.setEnabled(false);

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.save(usuario))
                .thenReturn(usuario);

        UserResponse resultado =
                service.cambiarEstado(
                        1L,
                        true
                );

        assertTrue(resultado.enabled());
    }

    @Test
    void disableDebeDeshabilitarUsuario() {
        UserAccount usuario = crearUsuario();

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        service.disable(1L);

        assertFalse(usuario.isEnabled());

        verify(repository).save(usuario);
    }

    private UserAccount crearUsuario() {
        UserAccount usuario = new UserAccount(
                "admin",
                "admin@vetcontrol.cl",
                "password-hash",
                Role.ADMIN,
                true
        );

        usuario.setId(1L);
        usuario.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        6,
                        1,
                        10,
                        0
                )
        );

        return usuario;
    }
}