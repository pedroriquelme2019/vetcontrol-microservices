package cl.duoc.vetcontrol.historial.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class FeignSecurityConfig {

    @Bean
    public RequestInterceptor bearerTokenForwarder() {

        return template -> {

            Object attributes =
                    RequestContextHolder.getRequestAttributes();

            if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
                return;
            }

            HttpServletRequest request =
                    servletAttributes.getRequest();

            String authorization =
                    request.getHeader("Authorization");

            if (authorization != null
                    && !authorization.isBlank()) {

                template.header(
                        "Authorization",
                        authorization
                );
            }
        };
    }
}