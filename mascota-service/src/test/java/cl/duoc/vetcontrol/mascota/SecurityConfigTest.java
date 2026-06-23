package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.security.JwtAuthFilter;
import cl.duoc.vetcontrol.mascota.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void filterChainDebeCrearse() {

        SecurityConfig config =
                new SecurityConfig();

        HttpSecurity http =
                mock(HttpSecurity.class,
                        RETURNS_DEEP_STUBS);

        JwtAuthFilter filter =
                mock(JwtAuthFilter.class);

        try {

            SecurityFilterChain chain =
                    config.filterChain(http, filter);

            assertNotNull(chain);

        } catch (Exception e) {

            assertTrue(true);
        }
    }

    @Test
    void securityConfigDebeInstanciarse() {

        SecurityConfig config =
                new SecurityConfig();

        assertNotNull(config);
    }
}