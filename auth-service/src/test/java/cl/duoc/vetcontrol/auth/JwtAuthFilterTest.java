package cl.duoc.vetcontrol.auth;

import cl.duoc.vetcontrol.auth.security.JwtAuthFilter;
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

        response =
                new MockHttpServletResponse();

        chain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sinTokenDebeContinuarSinAutenticar()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

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

        verify(chain)
                .doFilter(request, response);
    }

    @Test
    void headerNoBearerNoDebeAutenticar()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic credenciales"
        );

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
    void tokenInvalidoDebeLimpiarContexto()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-invalido"
        );

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
    void tokenSinRolNoDebeAutenticar()
            throws Exception {

        String token =
                crearTokenSinRol("admin");

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

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
    void tokenValidoDebeAutenticarAdministrador()
            throws Exception {

        String token =
                crearToken(
                        "admin",
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
                chain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertAll(
                () -> assertEquals(
                        "admin",
                        authentication.getName()
                ),
                () -> assertTrue(
                        authentication
                                .getAuthorities()
                                .stream()
                                .anyMatch(authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                                )
                )
        );
    }

    @Test
    void autenticacionExistenteNoDebeSerReemplazada()
            throws Exception {

        Authentication existente =
                new UsernamePasswordAuthenticationToken(
                        "usuario-existente",
                        null,
                        List.of()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(existente);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-invalido"
        );

        filter.doFilter(
                request,
                response,
                chain
        );

        assertSame(
                existente,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
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

    private String crearTokenSinRol(
            String username
    ) {
        Key key = Keys.hmacShaKeyFor(
                SECRET.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        return Jwts.builder()
                .setSubject(username)
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }
}