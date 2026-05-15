package cl.duoc.vetcontrol.auth.service;

import cl.duoc.vetcontrol.auth.dto.AuthResponse;
import cl.duoc.vetcontrol.auth.dto.LoginRequest;
import cl.duoc.vetcontrol.auth.dto.UserRequest;
import cl.duoc.vetcontrol.auth.exception.BusinessException;
import cl.duoc.vetcontrol.auth.model.UserAccount;
import cl.duoc.vetcontrol.auth.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = repository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Credenciales inválidas");
        }
        log.info("Login correcto para usuario {} con rol {}", user.getUsername(), user.getRole());
        return new AuthResponse(jwtService.generateToken(user), "Bearer", user.getUsername(), user.getRole());
    }

    public UserAccount create(UserRequest request) {
        if (repository.existsByUsername(request.username())) throw new BusinessException("El username ya existe");
        if (repository.existsByEmail(request.email())) throw new BusinessException("El correo ya existe");
        UserAccount user = new UserAccount(request.username(), request.email(), passwordEncoder.encode(request.password()), request.role(), true);
        return repository.save(user);
    }

    public List<UserAccount> findAll() { return repository.findAll(); }
}
