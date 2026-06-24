package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.security.JwtAuthFilter;
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
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        filter = new JwtAuthFilter();

        ReflectionTestUtils.setField(
                filter,
                "secret",
                SECRET
        );

        response =
                new MockHttpServletResponse();

        filterChain =
                mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void sinAuthorizationDebeContinuarSinAutenticar() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void headerQueNoSeaBearerDebeContinuarSinAutenticar()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic usuario:clave"
        );

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void tokenInvalidoDebeLimpiarContextoYContinuar()
            throws Exception {

        Authentication autenticacionAnterior =
                new UsernamePasswordAuthenticationToken(
                        "usuario-anterior",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(autenticacionAnterior);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-invalido"
        );

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void tokenValidoDebeAutenticarUsuarioConRol()
            throws Exception {

        String token =
                crearTokenValido(
                        "admin@vetcontrol.cl",
                        "ADMIN"
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        filter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        assertEquals(
                "admin@vetcontrol.cl",
                authentication.getName()
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        )
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    private String crearTokenValido(
            String username,
            String role
    ) {
        Key key =
                Keys.hmacShaKeyFor(
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