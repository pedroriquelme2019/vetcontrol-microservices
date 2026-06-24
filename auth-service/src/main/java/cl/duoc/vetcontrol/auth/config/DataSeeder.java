package cl.duoc.vetcontrol.auth.config;

import cl.duoc.vetcontrol.auth.model.Role;
import cl.duoc.vetcontrol.auth.model.UserAccount;
import cl.duoc.vetcontrol.auth.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.seed.recepcion-password:recepcion123}")
    private String recepcionPassword;

    @Value("${app.seed.veterinario-password:vet123}")
    private String veterinarioPassword;

    @Bean
    public CommandLineRunner seedUsers(
            UserAccountRepository repository,
            PasswordEncoder encoder
    ) {
        return args -> {

            if (!seedEnabled) {
                return;
            }

            crearSiNoExiste(
                    repository,
                    encoder,
                    "admin",
                    "admin@vetcontrol.cl",
                    adminPassword,
                    Role.ADMIN
            );

            crearSiNoExiste(
                    repository,
                    encoder,
                    "recepcion",
                    "recepcion@vetcontrol.cl",
                    recepcionPassword,
                    Role.RECEPCIONISTA
            );

            crearSiNoExiste(
                    repository,
                    encoder,
                    "vet",
                    "vet@vetcontrol.cl",
                    veterinarioPassword,
                    Role.VETERINARIO
            );
        };
    }

    private void crearSiNoExiste(
            UserAccountRepository repository,
            PasswordEncoder encoder,
            String username,
            String email,
            String password,
            Role role
    ) {
        if (repository.existsByUsernameIgnoreCase(
                username
        )) {
            return;
        }

        UserAccount user =
                new UserAccount(
                        username,
                        email,
                        encoder.encode(password),
                        role,
                        true
                );

        repository.save(user);
    }
}