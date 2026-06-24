package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.client.ClienteClient;
import cl.duoc.vetcontrol.venta.client.InventarioClient;
import cl.duoc.vetcontrol.venta.client.ProductoClient;
import cl.duoc.vetcontrol.venta.config.KafkaConfig;
import cl.duoc.vetcontrol.venta.dto.DetalleVentaRequest;
import cl.duoc.vetcontrol.venta.dto.ProductoDto;
import cl.duoc.vetcontrol.venta.dto.VentaRequest;
import cl.duoc.vetcontrol.venta.exception.BusinessException;
import cl.duoc.vetcontrol.venta.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.venta.model.DetalleVenta;
import cl.duoc.vetcontrol.venta.model.EstadoVenta;
import cl.duoc.vetcontrol.venta.model.Venta;
import cl.duoc.vetcontrol.venta.repository.VentaRepository;
import cl.duoc.vetcontrol.venta.service.VentaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VentaServiceTest {

    private VentaRepository repository;
    private ClienteClient clienteClient;
    private ProductoClient productoClient;
    private InventarioClient inventarioClient;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    private VentaService service;

    @BeforeEach
    void setUp() {
        repository = mock(VentaRepository.class);
        clienteClient = mock(ClienteClient.class);
        productoClient = mock(ProductoClient.class);
        inventarioClient = mock(InventarioClient.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = mock(ObjectMapper.class);

        service = new VentaService(
                repository,
                clienteClient,
                productoClient,
                inventarioClient,
                kafkaTemplate,
                objectMapper
        );
    }

    @Test
    void findAllDebeRetornarVentasOrdenadas() {
        when(repository.findAllByOrderByFechaDesc())
                .thenReturn(List.of(crearVentaRegistrada()));

        List<Venta> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals(EstadoVenta.REGISTRADA, resultado.get(0).getEstado());

        verify(repository).findAllByOrderByFechaDesc();
    }

    @Test
    void findByIdDebeRetornarVenta() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(crearVentaRegistrada()));

        Venta resultado = service.findById(1L);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(10L, resultado.getClienteId()),
                () -> assertEquals(EstadoVenta.REGISTRADA, resultado.getEstado())
        );
    }

    @Test
    void findByIdDebeFallarCuandoNoExiste() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(99L)
        );

        assertEquals(
                "Venta no encontrada: 99",
                exception.getMessage()
        );
    }

    @Test
    void findByClienteDebeRetornarVentasOrdenadas() {
        when(repository.findByClienteIdOrderByFechaDesc(10L))
                .thenReturn(List.of(crearVentaRegistrada()));

        List<Venta> resultado = service.findByCliente(10L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getClienteId());

        verify(repository)
                .findByClienteIdOrderByFechaDesc(10L);
    }

    @Test
    void createDebeRegistrarVentaYPublicarEvento()
            throws Exception {

        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        new BigDecimal("2500.00"),
                        true
                ));

        when(inventarioClient.validarStock(100L, 2))
                .thenReturn(true);

        configurarGuardadoExitoso();

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"tipo\":\"VENTA_CREADA\"}");

        CompletableFuture<SendResult<String, String>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_VENTA_CREADA),
                anyString()
        )).thenReturn(future);

        Venta resultado = service.create(crearRequestSimple());

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(10L, resultado.getClienteId()),
                () -> assertEquals("EFECTIVO", resultado.getMedioPago()),
                () -> assertEquals(
                        0,
                        resultado.getTotal()
                                .compareTo(new BigDecimal("5000.00"))
                ),
                () -> assertEquals(
                        EstadoVenta.REGISTRADA,
                        resultado.getEstado()
                ),
                () -> assertEquals(1, resultado.getDetalles().size())
        );

        DetalleVenta detalle = resultado.getDetalles().get(0);

        assertAll(
                () -> assertSame(resultado, detalle.getVenta()),
                () -> assertEquals(100L, detalle.getProductoId()),
                () -> assertEquals(2, detalle.getCantidad()),
                () -> assertEquals(
                        0,
                        detalle.getPrecioUnitario()
                                .compareTo(new BigDecimal("2500.00"))
                ),
                () -> assertEquals(
                        0,
                        detalle.getSubtotal()
                                .compareTo(new BigDecimal("5000.00"))
                )
        );

        verify(inventarioClient)
                .descontarStock(100L, 2);

        verify(repository, times(2))
                .save(any(Venta.class));

        ArgumentCaptor<Object> eventoCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(objectMapper)
                .writeValueAsString(eventoCaptor.capture());

        assertInstanceOf(Map.class, eventoCaptor.getValue());

        Map<?, ?> evento =
                (Map<?, ?>) eventoCaptor.getValue();

        assertAll(
                () -> assertEquals(
                        "VENTA_CREADA",
                        evento.get("tipo")
                ),
                () -> assertEquals(1L, evento.get("ventaId")),
                () -> assertEquals(10L, evento.get("clienteId")),
                () -> assertEquals("EFECTIVO", evento.get("medioPago")),
                () -> assertEquals("REGISTRADA", evento.get("estado"))
        );

        verify(kafkaTemplate).send(
                KafkaConfig.TOPIC_VENTA_CREADA,
                "{\"tipo\":\"VENTA_CREADA\"}"
        );
    }

    @Test
    void createDebeAgruparProductosRepetidos()
            throws Exception {

        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        new BigDecimal("1000.00"),
                        true
                ));

        when(inventarioClient.validarStock(100L, 5))
                .thenReturn(true);

        configurarGuardadoExitoso();
        configurarKafkaExitoso();

        VentaRequest request = new VentaRequest(
                10L,
                "DEBITO",
                List.of(
                        new DetalleVentaRequest(100L, 2),
                        new DetalleVentaRequest(100L, 3)
                )
        );

        Venta resultado = service.create(request);

        assertAll(
                () -> assertEquals(1, resultado.getDetalles().size()),
                () -> assertEquals(
                        5,
                        resultado.getDetalles().get(0).getCantidad()
                ),
                () -> assertEquals(
                        0,
                        resultado.getTotal()
                                .compareTo(new BigDecimal("5000.00"))
                )
        );

        verify(productoClient, times(1))
                .findById(100L);

        verify(inventarioClient)
                .validarStock(100L, 5);

        verify(inventarioClient)
                .descontarStock(100L, 5);
    }

    @Test
    void createDebeNormalizarMedioPago() throws Exception {
        configurarVentaSimpleExitosa();

        VentaRequest request = new VentaRequest(
                10L,
                "  transferencia  ",
                List.of(
                        new DetalleVentaRequest(100L, 2)
                )
        );

        Venta resultado = service.create(request);

        assertEquals(
                "TRANSFERENCIA",
                resultado.getMedioPago()
        );
    }

    @Test
    void createDebeRechazarMedioPagoNoPermitido() {
        configurarClienteValido();

        VentaRequest request = new VentaRequest(
                10L,
                "CHEQUE",
                List.of(
                        new DetalleVentaRequest(100L, 1)
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        assertEquals(
                "Medio de pago no permitido",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoClient,
                inventarioClient,
                repository,
                kafkaTemplate,
                objectMapper
        );
    }

    @Test
    void createDebeAceptarClienteSinCampoActivo()
            throws Exception {

        when(clienteClient.findById(10L))
                .thenReturn(Map.of("id", 10L));

        configurarProductoStockGuardadoYKafka();

        Venta resultado = service.create(crearRequestSimple());

        assertNotNull(resultado);
        assertEquals(EstadoVenta.REGISTRADA, resultado.getEstado());
    }

    @Test
    void createDebeRechazarClienteNulo() {
        when(clienteClient.findById(10L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "Cliente no existe o no está disponible",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoClient,
                inventarioClient,
                repository
        );
    }

    @Test
    void createDebeRechazarClienteVacio() {
        when(clienteClient.findById(10L))
                .thenReturn(Collections.emptyMap());

        assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        verifyNoInteractions(productoClient);
    }

    @Test
    void createDebeRechazarClienteInactivo() {
        when(clienteClient.findById(10L))
                .thenReturn(Map.of(
                        "id", 10L,
                        "activo", false
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "Cliente no existe o no está disponible",
                exception.getMessage()
        );
    }

    @Test
    void createDebeConvertirErrorFeignDelCliente() {
        when(clienteClient.findById(10L))
                .thenThrow(
                        crearFeignError(
                                404,
                                "/api/v1/clientes/10"
                        )
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "Cliente no existe o no está disponible",
                exception.getMessage()
        );
    }

    @Test
    void createDebeRechazarProductoNulo() {
        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "Producto no existe o no está disponible: 100",
                exception.getMessage()
        );

        verifyNoInteractions(inventarioClient);
    }

    @Test
    void createDebeRechazarProductoInactivo() {
        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        new BigDecimal("2500"),
                        false
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "Producto no existe o no está disponible: 100",
                exception.getMessage()
        );
    }

    @Test
    void createDebeConvertirErrorFeignDelProducto() {
        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenThrow(
                        crearFeignError(
                                404,
                                "/api/v1/productos/100"
                        )
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "Producto no existe o no está disponible: 100",
                exception.getMessage()
        );
    }

    @Test
    void createDebeRechazarPrecioNulo() {
        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        null,
                        true
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "El producto tiene un precio inválido: 100",
                exception.getMessage()
        );
    }

    @Test
    void createDebeRechazarPrecioCero() {
        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        BigDecimal.ZERO,
                        true
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "El producto tiene un precio inválido: 100",
                exception.getMessage()
        );
    }

    @Test
    void createDebeRechazarStockInsuficiente() {
        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        new BigDecimal("2500.00"),
                        true
                ));

        when(inventarioClient.validarStock(100L, 2))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "Stock insuficiente para producto 100",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void createDebeConvertirErrorFeignAlValidarStock() {
        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        new BigDecimal("2500.00"),
                        true
                ));

        when(inventarioClient.validarStock(100L, 2))
                .thenThrow(
                        crearFeignError(
                                503,
                                "/api/v1/inventario/productos/100/validar/2"
                        )
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(crearRequestSimple())
        );

        assertEquals(
                "Inventario no disponible para producto 100",
                exception.getMessage()
        );
    }

    @Test
    void createDebeCompensarPrimerProductoSiFallaSegundoDescuento() {
        configurarClienteValido();

        configurarProductoYStock(
                100L,
                new BigDecimal("1000.00"),
                2
        );

        configurarProductoYStock(
                200L,
                new BigDecimal("2000.00"),
                1
        );

        configurarGuardadoExitoso();

        doNothing()
                .when(inventarioClient)
                .descontarStock(100L, 2);

        doThrow(
                crearFeignError(
                        503,
                        "/api/v1/inventario/productos/200/descontar/1"
                )
        ).when(inventarioClient)
                .descontarStock(200L, 1);

        VentaRequest request = new VentaRequest(
                10L,
                "EFECTIVO",
                List.of(
                        new DetalleVentaRequest(100L, 2),
                        new DetalleVentaRequest(200L, 1)
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        assertEquals(
                "No fue posible descontar stock del producto 200",
                exception.getMessage()
        );

        verify(inventarioClient)
                .reponerStock(100L, 2);

        verify(inventarioClient, never())
                .reponerStock(200L, 1);

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createDebeCompensarTodosLosProductosSiFallaSegundoGuardado() {
        configurarClienteValido();

        configurarProductoYStock(
                100L,
                new BigDecimal("1000.00"),
                2
        );

        configurarProductoYStock(
                200L,
                new BigDecimal("2000.00"),
                1
        );

        AtomicInteger llamadas =
                new AtomicInteger();

        when(repository.save(any(Venta.class)))
                .thenAnswer(invocation -> {
                    Venta venta = invocation.getArgument(0);

                    if (llamadas.incrementAndGet() == 1) {
                        venta.setId(1L);
                        return venta;
                    }

                    throw new RuntimeException(
                            "Error al confirmar venta"
                    );
                });

        VentaRequest request = new VentaRequest(
                10L,
                "CREDITO",
                List.of(
                        new DetalleVentaRequest(100L, 2),
                        new DetalleVentaRequest(200L, 1)
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        assertEquals(
                "No fue posible completar la venta",
                exception.getMessage()
        );

        verify(inventarioClient)
                .reponerStock(100L, 2);

        verify(inventarioClient)
                .reponerStock(200L, 1);

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createDebeMantenerErrorOriginalAunqueCompensacionFalle() {
        configurarClienteValido();

        configurarProductoYStock(
                100L,
                new BigDecimal("1000.00"),
                2
        );

        configurarProductoYStock(
                200L,
                new BigDecimal("2000.00"),
                1
        );

        configurarGuardadoExitoso();

        doThrow(
                crearFeignError(
                        503,
                        "/inventario/descontar/200"
                )
        ).when(inventarioClient)
                .descontarStock(200L, 1);

        doThrow(new RuntimeException("Reposición fallida"))
                .when(inventarioClient)
                .reponerStock(100L, 2);

        VentaRequest request = new VentaRequest(
                10L,
                "EFECTIVO",
                List.of(
                        new DetalleVentaRequest(100L, 2),
                        new DetalleVentaRequest(200L, 1)
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        assertEquals(
                "No fue posible descontar stock del producto 200",
                exception.getMessage()
        );

        verify(inventarioClient)
                .reponerStock(100L, 2);
    }

    @Test
    void createDebeContinuarSiKafkaFallaAsincronamente()
            throws Exception {

        configurarVentaSimpleSinKafka();

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        CompletableFuture<SendResult<String, String>> future =
                new CompletableFuture<>();

        future.completeExceptionally(
                new RuntimeException("Kafka no disponible")
        );

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_VENTA_CREADA),
                anyString()
        )).thenReturn(future);

        Venta resultado = service.create(crearRequestSimple());

        assertNotNull(resultado);
        assertEquals(EstadoVenta.REGISTRADA, resultado.getEstado());
    }

    @Test
    void createDebeContinuarSiJsonFalla()
            throws Exception {

        configurarVentaSimpleSinKafka();

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(
                        new JsonProcessingException(
                                "Error JSON"
                        ) {
                        }
                );

        Venta resultado = service.create(crearRequestSimple());

        assertNotNull(resultado);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createDebeContinuarSiKafkaLanzaExcepcionDirecta()
            throws Exception {

        configurarVentaSimpleSinKafka();

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        when(kafkaTemplate.send(
                eq(KafkaConfig.TOPIC_VENTA_CREADA),
                anyString()
        )).thenThrow(
                new RuntimeException(
                        "Kafka detenido"
                )
        );

        Venta resultado = service.create(crearRequestSimple());

        assertNotNull(resultado);
        assertEquals(EstadoVenta.REGISTRADA, resultado.getEstado());
    }

    private void configurarClienteValido() {
        when(clienteClient.findById(10L))
                .thenReturn(Map.of(
                        "id", 10L,
                        "activo", true
                ));
    }

    private void configurarProductoYStock(
            Long productoId,
            BigDecimal precio,
            Integer cantidad
    ) {
        when(productoClient.findById(productoId))
                .thenReturn(crearProducto(
                        productoId,
                        precio,
                        true
                ));

        when(inventarioClient.validarStock(
                productoId,
                cantidad
        )).thenReturn(true);
    }

    private void configurarGuardadoExitoso() {
        when(repository.save(any(Venta.class)))
                .thenAnswer(invocation -> {
                    Venta venta = invocation.getArgument(0);

                    if (venta.getId() == null) {
                        venta.setId(1L);
                    }

                    return venta;
                });
    }

    private void configurarKafkaExitoso()
            throws Exception {

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        when(kafkaTemplate.send(
                anyString(),
                anyString()
        )).thenReturn(
                CompletableFuture.completedFuture(null)
        );
    }

    private void configurarProductoStockGuardadoYKafka()
            throws Exception {

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        new BigDecimal("2500.00"),
                        true
                ));

        when(inventarioClient.validarStock(100L, 2))
                .thenReturn(true);

        configurarGuardadoExitoso();
        configurarKafkaExitoso();
    }

    private void configurarVentaSimpleExitosa()
            throws Exception {

        configurarClienteValido();
        configurarProductoStockGuardadoYKafka();
    }

    private void configurarVentaSimpleSinKafka() {
        configurarClienteValido();

        when(productoClient.findById(100L))
                .thenReturn(crearProducto(
                        100L,
                        new BigDecimal("2500.00"),
                        true
                ));

        when(inventarioClient.validarStock(100L, 2))
                .thenReturn(true);

        configurarGuardadoExitoso();
    }

    private VentaRequest crearRequestSimple() {
        return new VentaRequest(
                10L,
                "EFECTIVO",
                List.of(
                        new DetalleVentaRequest(100L, 2)
                )
        );
    }

    private ProductoDto crearProducto(
            Long id,
            BigDecimal precio,
            boolean activo
    ) {
        return new ProductoDto(
                id,
                "Producto " + id,
                "MEDICAMENTO",
                precio,
                false,
                activo
        );
    }

    private Venta crearVentaRegistrada() {
        Venta venta = new Venta();

        venta.setId(1L);
        venta.setClienteId(10L);
        venta.setMedioPago("EFECTIVO");
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(new BigDecimal("5000.00"));
        venta.setEstado(EstadoVenta.REGISTRADA);

        return venta;
    }

    private FeignException crearFeignError(
            int estado,
            String url
    ) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                url,
                Collections
                        .<String, Collection<String>>emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        Response response = Response.builder()
                .status(estado)
                .reason("Error remoto")
                .request(request)
                .headers(
                        Collections
                                .<String, Collection<String>>emptyMap()
                )
                .build();

        return FeignException.errorStatus(
                "ClienteFeign#operacion",
                response
        );
    }
}