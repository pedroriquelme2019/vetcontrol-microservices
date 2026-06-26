package cl.duoc.vetcontrol.gateway.filter;

import cl.duoc.vetcontrol.gateway.dto.GatewayErrorResponse;
import cl.duoc.vetcontrol.gateway.dto.JwtUserData;
import cl.duoc.vetcontrol.gateway.service.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationFilter
        implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(
                    JwtAuthenticationFilter.class
            );

    private static final String HEADER_USUARIO =
            "X-Authenticated-User";

    private static final String HEADER_ROL =
            "X-Authenticated-Role";

    private static final String HEADER_USER_ID =
            "X-Authenticated-User-Id";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            ObjectMapper objectMapper
    ) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        ServerHttpRequest request =
                exchange.getRequest();

        String path =
                request.getURI()
                        .getPath();

        /*
         * Las rutas públicas pueden continuar sin JWT.
         */
        if (esRutaPublica(request, path)) {
            return chain.filter(exchange);
        }

        /*
         * Las rutas que no comienzan con /api/
         * tampoco necesitan validación JWT.
         */
        if (!esRutaProtegida(path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader =
                request.getHeaders()
                        .getFirst(
                                HttpHeaders.AUTHORIZATION
                        );

        /*
         * Verifica que exista el encabezado Authorization
         * y que utilice el formato Bearer.
         */
        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith("Bearer ")) {

            return escribirError(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere un token de autenticación"
            );
        }

        String token =
                authorizationHeader
                        .substring(7)
                        .trim();

        if (token.isBlank()) {
            return escribirError(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere un token de autenticación"
            );
        }

        try {
            /*
             * JwtService valida la firma, la expiración
             * y los datos obligatorios del token.
             */
            JwtUserData usuario =
                    jwtService.validarYObtenerDatos(
                            token
                    );

            /*
             * La administración de usuarios está reservada
             * exclusivamente para administradores.
             */
            if (esRutaAdministracionUsuarios(path)
                    && !jwtService.tieneRol(
                    usuario,
                    "ADMIN"
            )) {
                return escribirError(
                        exchange,
                        HttpStatus.FORBIDDEN,
                        "No tiene permisos para acceder a este recurso"
                );
            }

            /*
             * Se eliminan los encabezados enviados por el cliente
             * para impedir la suplantación de identidad.
             */
            ServerHttpRequest requestAutenticado =
                    request.mutate()
                            .headers(headers -> {

                                headers.remove(
                                        HEADER_USUARIO
                                );

                                headers.remove(
                                        HEADER_ROL
                                );

                                headers.remove(
                                        HEADER_USER_ID
                                );

                                headers.set(
                                        HEADER_USUARIO,
                                        usuario.getUsername()
                                );

                                headers.set(
                                        HEADER_ROL,
                                        usuario.getRole()
                                );

                                if (usuario.getUserId() != null) {
                                    headers.set(
                                            HEADER_USER_ID,
                                            String.valueOf(
                                                    usuario.getUserId()
                                            )
                                    );
                                }
                            })
                            .build();

            ServerWebExchange exchangeAutenticado =
                    exchange.mutate()
                            .request(
                                    requestAutenticado
                            )
                            .build();

            log.debug(
                    "Solicitud autenticada usuario={} rol={} path={}",
                    usuario.getUsername(),
                    usuario.getRole(),
                    path
            );

            return chain.filter(
                    exchangeAutenticado
            );

        } catch (Exception exception) {
            log.warn(
                    "Token rechazado en path={}: {}",
                    path,
                    exception.getClass()
                            .getSimpleName()
            );

            return escribirError(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Token inválido o expirado"
            );
        }
    }

    private boolean esRutaPublica(
            ServerHttpRequest request,
            String path
    ) {
        if (HttpMethod.OPTIONS.equals(
                request.getMethod()
        )) {
            return true;
        }

        return path.equals(
                "/api/v1/auth/login"
        )
                || path.equals(
                "/api/v1/auth/login/"
        )
                || path.startsWith(
                "/swagger-ui"
        )
                || path.startsWith(
                "/v3/api-docs"
        )
                || path.startsWith(
                "/webjars"
        )
                || path.equals(
                "/actuator/health"
        )
                || path.equals(
                "/actuator/info"
        )
                || path.equals(
                "/favicon.ico"
        );
    }

    private boolean esRutaProtegida(
            String path
    ) {
        return path.startsWith(
                "/api/"
        );
    }

    private boolean esRutaAdministracionUsuarios(
            String path
    ) {
        return path.equals(
                "/api/v1/users"
        ) || path.startsWith(
                "/api/v1/users/"
        );
    }

    private Mono<Void> escribirError(
            ServerWebExchange exchange,
            HttpStatus status,
            String message
    ) {
        ServerHttpResponse response =
                exchange.getResponse();

        /*
         * Evita intentar escribir nuevamente una respuesta
         * que ya fue enviada.
         */
        if (response.isCommitted()) {
            return Mono.empty();
        }

        response.setStatusCode(status);

        response.getHeaders()
                .setContentType(
                        MediaType.APPLICATION_JSON
                );

        GatewayErrorResponse errorResponse =
                new GatewayErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        exchange.getRequest()
                                .getURI()
                                .getPath()
                );

        byte[] contenido =
                convertirAJson(
                        errorResponse
                );

        response.getHeaders()
                .setContentLength(
                        contenido.length
                );

        DataBuffer buffer =
                response.bufferFactory()
                        .wrap(contenido);

        return response.writeWith(
                Mono.just(buffer)
        );
    }

    private byte[] convertirAJson(
            GatewayErrorResponse errorResponse
    ) {
        try {
            return objectMapper
                    .writeValueAsBytes(
                            errorResponse
                    );

        } catch (JsonProcessingException exception) {
            String jsonAlternativo =
                    """
                    {
                      "status": 500,
                      "error": "Internal Server Error",
                      "message": "No fue posible procesar la respuesta",
                      "path": ""
                    }
                    """;

            return jsonAlternativo.getBytes(
                    StandardCharsets.UTF_8
            );
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}

