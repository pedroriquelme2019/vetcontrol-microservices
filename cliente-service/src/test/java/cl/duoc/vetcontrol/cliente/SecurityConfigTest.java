package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.security.JwtAuthFilter;
import cl.duoc.vetcontrol.cliente.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void filterChainDebeConstruirseSinErrores() {

        SecurityConfig config = new SecurityConfig();

        HttpSecurity http =
                mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        JwtAuthFilter filter =
                mock(JwtAuthFilter.class);

        assertDoesNotThrow(() ->
                config.filterChain(http, filter)
        );
    }
}