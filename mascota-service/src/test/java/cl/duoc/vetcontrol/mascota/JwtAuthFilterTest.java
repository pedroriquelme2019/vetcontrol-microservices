package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.security.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Test
    void debeContinuarSinAuthorization() throws Exception {

        JwtAuthFilter filter =
                new JwtAuthFilter();

        Field secret =
                JwtAuthFilter.class.getDeclaredField("secret");

        secret.setAccessible(true);
        secret.set(filter,
                "12345678901234567890123456789012");

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void debeLimpiarContextoSiTokenEsInvalido() throws Exception {

        JwtAuthFilter filter =
                new JwtAuthFilter();

        Field secret =
                JwtAuthFilter.class.getDeclaredField("secret");

        secret.setAccessible(true);
        secret.set(filter,
                "12345678901234567890123456789012");

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-invalido"
        );

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }
}