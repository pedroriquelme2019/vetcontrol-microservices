package cl.duoc.vetcontrol.gateway;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cl.duoc.vetcontrol.gateway.dto.GatewayErrorResponse;
import cl.duoc.vetcontrol.gateway.dto.JwtUserData;
import cl.duoc.vetcontrol.gateway.filter.JwtAuthenticationFilter;
import cl.duoc.vetcontrol.gateway.service.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private ObjectMapper objectMapper;
    private GatewayFilterChain chain;
    private JwtAuthenticationFilter filter;


    @BeforeEach
    void setUp() {

        jwtService =
                mock(JwtService.class);

        objectMapper =
                new ObjectMapper();

        /*
         * Permite serializar LocalDateTime dentro
         * de GatewayErrorResponse.
         */
        objectMapper.registerModule(
                new JavaTimeModule()
        );

        chain =
                mock(GatewayFilterChain.class);

        when(chain.filter(
                any(ServerWebExchange.class)
        )).thenReturn(Mono.empty());

        filter =
                new JwtAuthenticationFilter(
                        jwtService,
                        objectMapper
                );
    }


    @Test
    void loginDebeSerRutaPublica() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .post(
                                        "/api/v1/auth/login"
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginConBarraFinalDebeSerPublico() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .post(
                                        "/api/v1/auth/login/"
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtService);
    }

    @Test
    void solicitudOptionsDebeSerPublica() {
        MockServerHttpRequest request =
                MockServerHttpRequest
                        .method(
                                HttpMethod.OPTIONS,
                                URI.create(
                                        "/api/v1/clientes"
                                )
                        )
                        .build();

        MockServerWebExchange exchange =
                crearExchange(request);

        filter.filter(
                exchange,
                chain
        ).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtService);
    }

    @Test
    void swaggerDebeSerPublico() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/swagger-ui/index.html"
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtService);
    }

    @Test
    void healthDebeSerPublico() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/actuator/health"
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtService);
    }

    @Test
    void rutaFueraDeApiDebeContinuarSinToken() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get("/")
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtService);
    }

    @Test
    void rutaProtegidaSinTokenDebeRetornar401() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/api/v1/clientes"
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exchange.getResponse()
                        .getStatusCode()
        );

        String body =
                exchange.getResponse()
                        .getBodyAsString()
                        .block();

        assertNotNull(body);
        assertTrue(
                body.contains(
                        "Se requiere un token de autenticación"
                )
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(chain);
    }

    @Test
    void headerNoBearerDebeRetornar401() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/api/v1/clientes"
                                )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Basic credenciales"
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exchange.getResponse()
                        .getStatusCode()
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(chain);
    }

    @Test
    void bearerVacioDebeRetornar401() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/api/v1/clientes"
                                )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer "
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exchange.getResponse()
                        .getStatusCode()
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(chain);
    }

    @Test
    void tokenInvalidoDebeRetornar401() {
        when(jwtService.validarYObtenerDatos(
                "token-invalido"
        )).thenThrow(
                new IllegalArgumentException(
                        "Token inválido"
                )
        );

        MockServerWebExchange exchange =
                crearExchange(
                        requestConToken(
                                "/api/v1/clientes",
                                "token-invalido"
                        )
                );

        filter.filter(
                exchange,
                chain
        ).block();

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exchange.getResponse()
                        .getStatusCode()
        );

        String body =
                exchange.getResponse()
                        .getBodyAsString()
                        .block();

        assertNotNull(body);
        assertTrue(
                body.contains(
                        "Token inválido o expirado"
                )
        );

        verifyNoInteractions(chain);
    }

    @Test
    void tokenValidoDebeAgregarHeadersInternos() {
        JwtUserData usuario =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        7L
                );

        when(jwtService.validarYObtenerDatos(
                "token-valido"
        )).thenReturn(usuario);

        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/api/v1/clientes"
                                )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer token-valido"
                                )
                                .header(
                                        "X-Authenticated-User",
                                        "usuario-falso"
                                )
                                .header(
                                        "X-Authenticated-Role",
                                        "ROL_FALSO"
                                )
                                .header(
                                        "X-Authenticated-User-Id",
                                        "999"
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        ArgumentCaptor<ServerWebExchange> captor =
                ArgumentCaptor.forClass(
                        ServerWebExchange.class
                );

        verify(chain).filter(captor.capture());

        ServerWebExchange enviado =
                captor.getValue();

        assertAll(
                () -> assertEquals(
                        "admin",
                        enviado.getRequest()
                                .getHeaders()
                                .getFirst(
                                        "X-Authenticated-User"
                                )
                ),
                () -> assertEquals(
                        "ADMIN",
                        enviado.getRequest()
                                .getHeaders()
                                .getFirst(
                                        "X-Authenticated-Role"
                                )
                ),
                () -> assertEquals(
                        "7",
                        enviado.getRequest()
                                .getHeaders()
                                .getFirst(
                                        "X-Authenticated-User-Id"
                                )
                )
        );
    }

    @Test
    void userIdNuloNoDebeAgregarHeaderUserId() {
        JwtUserData usuario =
                new JwtUserData(
                        "vet",
                        "VETERINARIO",
                        null
                );

        when(jwtService.validarYObtenerDatos(
                "token-vet"
        )).thenReturn(usuario);

        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/api/v1/mascotas"
                                )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer token-vet"
                                )
                                .header(
                                        "X-Authenticated-User-Id",
                                        "999"
                                )
                                .build()
                );

        filter.filter(
                exchange,
                chain
        ).block();

        ArgumentCaptor<ServerWebExchange> captor =
                ArgumentCaptor.forClass(
                        ServerWebExchange.class
                );

        verify(chain).filter(captor.capture());

        assertNull(
                captor.getValue()
                        .getRequest()
                        .getHeaders()
                        .getFirst(
                                "X-Authenticated-User-Id"
                        )
        );
    }

    @Test
    void recepcionistaNoDebeAdministrarUsuarios() {
        JwtUserData usuario =
                new JwtUserData(
                        "recepcion",
                        "RECEPCIONISTA",
                        2L
                );

        when(jwtService.validarYObtenerDatos(
                "token-recepcion"
        )).thenReturn(usuario);

        when(jwtService.tieneRol(
                usuario,
                "ADMIN"
        )).thenReturn(false);

        MockServerWebExchange exchange =
                crearExchange(
                        requestConToken(
                                "/api/v1/users",
                                "token-recepcion"
                        )
                );

        filter.filter(
                exchange,
                chain
        ).block();

        assertEquals(
                HttpStatus.FORBIDDEN,
                exchange.getResponse()
                        .getStatusCode()
        );

        String body =
                exchange.getResponse()
                        .getBodyAsString()
                        .block();

        assertNotNull(body);
        assertTrue(
                body.contains(
                        "No tiene permisos para acceder a este recurso"
                )
        );

        verifyNoInteractions(chain);
    }

    @Test
    void veterinarioNoDebeAccederASubRutaUsuarios() {
        JwtUserData usuario =
                new JwtUserData(
                        "vet",
                        "VETERINARIO",
                        3L
                );

        when(jwtService.validarYObtenerDatos(
                "token-vet"
        )).thenReturn(usuario);

        when(jwtService.tieneRol(
                usuario,
                "ADMIN"
        )).thenReturn(false);

        MockServerWebExchange exchange =
                crearExchange(
                        requestConToken(
                                "/api/v1/users/1",
                                "token-vet"
                        )
                );

        filter.filter(
                exchange,
                chain
        ).block();

        assertEquals(
                HttpStatus.FORBIDDEN,
                exchange.getResponse()
                        .getStatusCode()
        );

        verifyNoInteractions(chain);
    }

    @Test
    void administradorDebeAccederAUsuarios() {
        JwtUserData usuario =
                new JwtUserData(
                        "admin",
                        "ADMIN",
                        1L
                );

        when(jwtService.validarYObtenerDatos(
                "token-admin"
        )).thenReturn(usuario);

        when(jwtService.tieneRol(
                usuario,
                "ADMIN"
        )).thenReturn(true);

        MockServerWebExchange exchange =
                crearExchange(
                        requestConToken(
                                "/api/v1/users",
                                "token-admin"
                        )
                );

        filter.filter(
                exchange,
                chain
        ).block();

        verify(chain)
                .filter(
                        any(ServerWebExchange.class)
                );

        assertNull(
                exchange.getResponse()
                        .getStatusCode()
        );
    }

    @Test
    void respuestaComprometidaNoDebeEscribirseNuevamente() {
        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/api/v1/clientes"
                                )
                                .build()
                );

        exchange.getResponse()
                .setComplete()
                .block();

        assertTrue(
                exchange.getResponse()
                        .isCommitted()
        );

        assertDoesNotThrow(
                () -> filter.filter(
                        exchange,
                        chain
                ).block()
        );

        verifyNoInteractions(chain);
        verifyNoInteractions(jwtService);
    }

    @Test
    void errorAlSerializarDebeUsarJsonAlternativo()
            throws Exception {

        ObjectMapper mapperConError =
                mock(ObjectMapper.class);

        when(mapperConError.writeValueAsBytes(
                any(GatewayErrorResponse.class)
        )).thenThrow(
                new JsonProcessingException(
                        "Error de serialización"
                ) {
                }
        );

        JwtAuthenticationFilter filtroConError =
                new JwtAuthenticationFilter(
                        jwtService,
                        mapperConError
                );

        MockServerWebExchange exchange =
                crearExchange(
                        MockServerHttpRequest
                                .get(
                                        "/api/v1/clientes"
                                )
                                .build()
                );

        filtroConError.filter(
                exchange,
                chain
        ).block();

        String body =
                exchange.getResponse()
                        .getBodyAsString()
                        .block();

        assertNotNull(body);

        assertTrue(
                body.contains(
                        "No fue posible procesar la respuesta"
                )
        );
    }

    @Test
    void ordenDelFiltroDebeSerCorrecto() {
        assertEquals(
                Ordered.HIGHEST_PRECEDENCE + 10,
                filter.getOrder()
        );
    }

    private MockServerWebExchange crearExchange(
            MockServerHttpRequest request
    ) {
        return MockServerWebExchange.from(
                request
        );
    }

    private MockServerHttpRequest requestConToken(
            String path,
            String token
    ) {
        return MockServerHttpRequest
                .get(path)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .build();
    }
}