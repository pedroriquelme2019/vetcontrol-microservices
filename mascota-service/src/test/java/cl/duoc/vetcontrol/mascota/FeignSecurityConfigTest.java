package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.config.FeignSecurityConfig;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FeignSecurityConfigTest {

    @Test
    void bearerTokenForwarderDebeAgregarHeader() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer token123"
        );

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        FeignSecurityConfig config =
                new FeignSecurityConfig();

        RequestInterceptor interceptor =
                config.bearerTokenForwarder();

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertTrue(
                template.headers()
                        .containsKey("Authorization")
        );

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void bearerTokenForwarderSinRequestNoDebeFallar() {

        RequestContextHolder.resetRequestAttributes();

        FeignSecurityConfig config =
                new FeignSecurityConfig();

        RequestInterceptor interceptor =
                config.bearerTokenForwarder();

        RequestTemplate template =
                new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().isEmpty());
    }
}