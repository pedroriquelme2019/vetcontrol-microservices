package cl.duoc.vetcontrol.auth.repository;

import cl.duoc.vetcontrol.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(
            String username
    );

    Optional<UserAccount> findByEmailIgnoreCase(
            String email
    );

    Optional<UserAccount> findByUsernameIgnoreCaseOrEmailIgnoreCase(
            String username,
            String email
    );

    boolean existsByUsernameIgnoreCase(
            String username
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    List<UserAccount> findAllByOrderByUsernameAsc();
}