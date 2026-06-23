package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.security.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Test
    void jwtAuthFilterDebeInstanciarse() {

        JwtAuthFilter filter = new JwtAuthFilter();

        assertNotNull(filter);
    }

    @Test
    void doFilterSinAuthorizationDebeContinuar() throws Exception {

        JwtAuthFilter filter = new JwtAuthFilter();

        ReflectionTestUtils.setField(
                filter,
                "secret",
                "12345678901234567890123456789012"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterConBearerInvalidoDebeContinuar() throws Exception {

        JwtAuthFilter filter = new JwtAuthFilter();

        ReflectionTestUtils.setField(
                filter,
                "secret",
                "12345678901234567890123456789012"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token_invalido"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterConHeaderNoBearerDebeContinuar() throws Exception {

        JwtAuthFilter filter = new JwtAuthFilter();

        ReflectionTestUtils.setField(
                filter,
                "secret",
                "12345678901234567890123456789012"
        );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic admin"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        verify(chain).doFilter(request, response);
    }
}