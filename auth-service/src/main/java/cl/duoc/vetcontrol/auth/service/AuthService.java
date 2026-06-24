package cl.duoc.vetcontrol.auth.service;

import cl.duoc.vetcontrol.auth.dto.*;
import cl.duoc.vetcontrol.auth.exception.BusinessException;
import cl.duoc.vetcontrol.auth.exception.InvalidCredentialsException;
import cl.duoc.vetcontrol.auth.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.auth.model.UserAccount;
import cl.duoc.vetcontrol.auth.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AuthService.class
            );

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(
            LoginRequest request
    ) {
        String identificador =
                request.username().trim();

        UserAccount user =
                repository
                        .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                                identificador,
                                identificador
                        )
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Credenciales inválidas"
                                )
                        );

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException(
                    "Credenciales inválidas"
            );
        }

        boolean passwordCorrecta =
                passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                );

        if (!passwordCorrecta) {
            throw new InvalidCredentialsException(
                    "Credenciales inválidas"
            );
        }

        String token =
                jwtService.generateToken(user);

        log.info(
                "Login correcto para usuario={} rol={}",
                user.getUsername(),
                user.getRole()
        );

        return new AuthResponse(
                token,
                "Bearer",
                user.getUsername(),
                user.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return repository
                .findAllByOrderByUsernameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(
            Long id
    ) {
        return toResponse(
                obtenerUsuario(id)
        );
    }

    @Transactional
    public UserResponse create(
            UserRequest request
    ) {
        String username =
                normalizarUsername(
                        request.username()
                );

        String email =
                normalizarEmail(
                        request.email()
                );

        if (repository.existsByUsernameIgnoreCase(
                username
        )) {
            throw new BusinessException(
                    "El username ya existe"
            );
        }

        if (repository.existsByEmailIgnoreCase(
                email
        )) {
            throw new BusinessException(
                    "El correo ya existe"
            );
        }

        UserAccount user =
                new UserAccount(
                        username,
                        email,
                        passwordEncoder.encode(
                                request.password()
                        ),
                        request.role(),
                        true
                );

        UserAccount guardado =
                repository.save(user);

        log.info(
                "Usuario creado id={} username={} rol={}",
                guardado.getId(),
                guardado.getUsername(),
                guardado.getRole()
        );

        return toResponse(guardado);
    }

    @Transactional
    public UserResponse update(
            Long id,
            UserUpdateRequest request
    ) {
        UserAccount user =
                obtenerUsuario(id);

        if (request.email() != null) {
            String nuevoEmail =
                    normalizarEmail(
                            request.email()
                    );

            repository
                    .findByEmailIgnoreCase(
                            nuevoEmail
                    )
                    .filter(encontrado ->
                            !encontrado.getId().equals(id)
                    )
                    .ifPresent(encontrado -> {
                        throw new BusinessException(
                                "El correo ya existe"
                        );
                    });

            user.setEmail(nuevoEmail);
        }

        if (request.password() != null) {
            user.setPasswordHash(
                    passwordEncoder.encode(
                            request.password()
                    )
            );
        }

        if (request.role() != null) {
            user.setRole(
                    request.role()
            );
        }

        if (request.enabled() != null) {
            user.setEnabled(
                    request.enabled()
            );
        }

        UserAccount actualizado =
                repository.save(user);

        log.info(
                "Usuario actualizado id={} username={}",
                actualizado.getId(),
                actualizado.getUsername()
        );

        return toResponse(actualizado);
    }

    @Transactional
    public UserResponse cambiarEstado(
            Long id,
            boolean enabled
    ) {
        UserAccount user =
                obtenerUsuario(id);

        user.setEnabled(enabled);

        UserAccount actualizado =
                repository.save(user);

        log.info(
                "Estado de usuario actualizado id={} enabled={}",
                id,
                enabled
        );

        return toResponse(actualizado);
    }

    @Transactional
    public void disable(
            Long id
    ) {
        UserAccount user =
                obtenerUsuario(id);

        user.setEnabled(false);

        repository.save(user);

        log.info(
                "Usuario deshabilitado id={}",
                id
        );
    }

    private UserAccount obtenerUsuario(
            Long id
    ) {
        return repository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado: " + id
                        )
                );
    }

    private String normalizarUsername(
            String username
    ) {
        return username.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizarEmail(
            String email
    ) {
        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private UserResponse toResponse(
            UserAccount user
    ) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}