package cl.duoc.vetcontrol.auth.config;

import cl.duoc.vetcontrol.auth.model.UserAccount;
import cl.duoc.vetcontrol.auth.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedUsers(UserAccountRepository repository, PasswordEncoder encoder) {
        return args -> {
            if (!repository.existsByUsername("admin")) {
                repository.save(new UserAccount("admin", "admin@vetcontrol.cl", encoder.encode("admin123"), "ADMIN", true));
            }
            if (!repository.existsByUsername("recepcion")) {
                repository.save(new UserAccount("recepcion", "recepcion@vetcontrol.cl", encoder.encode("recepcion123"), "RECEPCIONISTA", true));
            }
            if (!repository.existsByUsername("vet")) {
                repository.save(new UserAccount("vet", "vet@vetcontrol.cl", encoder.encode("vet123"), "VETERINARIO", true));
            }
        };
    }
}
