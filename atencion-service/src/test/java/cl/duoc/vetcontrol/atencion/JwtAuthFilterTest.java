package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.security.JwtAuthFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private static final String SECRET =
            "vetcontrol-secret-key-2026-vetcontrol-secret-key-2026";

    private JwtAuthFilter filter;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        filter = new JwtAuthFilter();

        ReflectionTestUtils.setField(
                filter,
                "secret",
                SECRET
        );

        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sinHeaderDebeContinuarSinAutenticar()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        filter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(chain).doFilter(request, response);
    }

    @Test
    void headerNoBearerDebeContinuarSinAutenticar()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic usuario-clave"
        );

        filter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void tokenInvalidoDebeLimpiarContexto()
            throws Exception {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "usuario-anterior",
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                )
                        )
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-invalido"
        );

        filter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(chain).doFilter(request, response);
    }

    @Test
    void tokenValidoDebeAutenticarUsuario()
            throws Exception {

        String token = crearToken(
                "veterinario@vetcontrol.cl",
                "VETERINARIO"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        filter.doFilter(request, response, chain);

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertAll(
                () -> assertTrue(
                        authentication.isAuthenticated()
                ),
                () -> assertEquals(
                        "veterinario@vetcontrol.cl",
                        authentication.getName()
                ),
                () -> assertTrue(
                        authentication
                                .getAuthorities()
                                .stream()
                                .anyMatch(authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_VETERINARIO"
                                                )
                                )
                )
        );

        verify(chain).doFilter(request, response);
    }

    private String crearToken(
            String username,
            String role
    ) {
        Key key = Keys.hmacShaKeyFor(
                SECRET.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }
}