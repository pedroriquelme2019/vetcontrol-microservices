package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.config.FeignSecurityConfig;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

class FeignSecurityConfigTest {

    private RequestInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new FeignSecurityConfig()
                .bearerTokenForwarder();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void sinContextoHttpNoDebeAgregarToken() {
        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertFalse(
                template.headers()
                        .containsKey("Authorization")
        );
    }

    @Test
    void requestSinTokenNoDebeAgregarHeader() {
        establecerRequest(
                new MockHttpServletRequest()
        );

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertFalse(
                template.headers()
                        .containsKey("Authorization")
        );
    }

    @Test
    void tokenVacioNoDebeAgregarse() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "   "
        );

        establecerRequest(request);

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertFalse(
                template.headers()
                        .containsKey("Authorization")
        );
    }

    @Test
    void bearerTokenDebeReenviarse() {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token-venta"
        );

        establecerRequest(request);

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertTrue(
                template.headers()
                        .get("Authorization")
                        .contains("Bearer token-venta")
        );
    }

    private void establecerRequest(
            MockHttpServletRequest request
    ) {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }
}