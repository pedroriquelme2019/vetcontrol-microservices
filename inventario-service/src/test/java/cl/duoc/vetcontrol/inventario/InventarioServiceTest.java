package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.client.ProductoClient;
import cl.duoc.vetcontrol.inventario.dto.InventarioRequest;
import cl.duoc.vetcontrol.inventario.dto.InventarioUpdateRequest;
import cl.duoc.vetcontrol.inventario.exception.BusinessException;
import cl.duoc.vetcontrol.inventario.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import cl.duoc.vetcontrol.inventario.repository.InventarioRepository;
import cl.duoc.vetcontrol.inventario.service.InventarioService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventarioServiceTest {

    private InventarioRepository repository;
    private ProductoClient productoClient;
    private InventarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(InventarioRepository.class);
        productoClient = mock(ProductoClient.class);

        service = new InventarioService(
                repository,
                productoClient
        );
    }

    @Test
    void findAllDebeRetornarSoloInventariosActivos() {

        when(repository.findByActivoTrue())
                .thenReturn(List.of(crearItem()));

        List<InventarioItem> resultado =
                service.findAll();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isActivo());
        assertEquals(100L, resultado.get(0).getProductoId());

        verify(repository).findByActivoTrue();
    }

    @Test
    void findByProductoIdDebeRetornarInventarioActivo() {

        when(repository.findByProductoIdAndActivoTrue(100L))
                .thenReturn(Optional.of(crearItem()));

        InventarioItem resultado =
                service.findByProductoId(100L);

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(100L, resultado.getProductoId()),
                () -> assertEquals(10, resultado.getStockActual()),
                () -> assertTrue(resultado.isActivo())
        );
    }

    @Test
    void findByProductoIdDebeFallarCuandoNoExiste() {

        when(repository.findByProductoIdAndActivoTrue(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findByProductoId(999L)
                );

        assertEquals(
                "Inventario no encontrado para producto: 999",
                exception.getMessage()
        );
    }

    @Test
    void createDebeGuardarInventarioNuevo() {

        configurarProductoValido();

        when(repository.findByProductoId(100L))
                .thenReturn(Optional.empty());

        when(repository.save(any(InventarioItem.class)))
                .thenAnswer(invocation -> {
                    InventarioItem item =
                            invocation.getArgument(0);

                    item.setId(1L);

                    return item;
                });

        InventarioItem resultado =
                service.create(crearRequest());

        assertAll(
                () -> assertEquals(1L, resultado.getId()),
                () -> assertEquals(100L, resultado.getProductoId()),
                () -> assertEquals(10, resultado.getStockActual()),
                () -> assertEquals(3, resultado.getStockMinimo()),
                () -> assertTrue(resultado.isActivo())
        );

        verify(productoClient).findById(100L);
        verify(repository).findByProductoId(100L);
        verify(repository).save(any(InventarioItem.class));
    }

    @Test
    void createDebeRechazarInventarioActivoDuplicado() {

        configurarProductoValido();

        when(repository.findByProductoId(100L))
                .thenReturn(Optional.of(crearItem()));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "Ya existe inventario para el producto: 100",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any(InventarioItem.class));
    }

    @Test
    void createDebeReactivarInventarioInactivo() {

        configurarProductoValido();

        InventarioItem inactivo = crearItem();
        inactivo.setId(8L);
        inactivo.setActivo(false);
        inactivo.setStockActual(0);
        inactivo.setStockMinimo(0);

        when(repository.findByProductoId(100L))
                .thenReturn(Optional.of(inactivo));

        when(repository.save(any(InventarioItem.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        InventarioItem resultado =
                service.create(crearRequest());

        assertAll(
                () -> assertEquals(8L, resultado.getId()),
                () -> assertTrue(resultado.isActivo()),
                () -> assertEquals(10, resultado.getStockActual()),
                () -> assertEquals(3, resultado.getStockMinimo())
        );

        verify(repository).save(inactivo);
    }

    @Test
    void createDebeAceptarProductoSinCampoActivo() {

        when(productoClient.findById(100L))
                .thenReturn(Map.of(
                        "id",
                        100L
                ));

        when(repository.findByProductoId(100L))
                .thenReturn(Optional.empty());

        when(repository.save(any(InventarioItem.class)))
                .thenAnswer(invocation -> {
                    InventarioItem item =
                            invocation.getArgument(0);

                    item.setId(1L);

                    return item;
                });

        InventarioItem resultado =
                service.create(crearRequest());

        assertNotNull(resultado);
        assertTrue(resultado.isActivo());
    }

    @Test
    void createDebeRechazarProductoNulo() {

        when(productoClient.findById(100L))
                .thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "El producto no existe o no está disponible",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void createDebeRechazarProductoVacio() {

        when(productoClient.findById(100L))
                .thenReturn(Collections.emptyMap());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "El producto no existe o no está disponible",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void createDebeRechazarProductoInactivo() {

        when(productoClient.findById(100L))
                .thenReturn(Map.of(
                        "id", 100L,
                        "activo", false
                ));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "El producto no existe o no está disponible",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void createDebeConvertirErrorFeignEnBusinessException() {

        when(productoClient.findById(100L))
                .thenThrow(
                        crearFeign404(
                                "/api/v1/productos/100"
                        )
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(crearRequest())
                );

        assertEquals(
                "El producto no existe o no está disponible",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void updateDebeModificarStockActualYMinimo() {

        InventarioItem existente = crearItem();

        InventarioUpdateRequest request =
                new InventarioUpdateRequest(
                        25,
                        5
                );

        when(repository.findByProductoIdAndActivoTrue(100L))
                .thenReturn(Optional.of(existente));

        when(repository.save(any(InventarioItem.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        InventarioItem resultado =
                service.update(
                        100L,
                        request
                );

        assertAll(
                () -> assertEquals(25, resultado.getStockActual()),
                () -> assertEquals(5, resultado.getStockMinimo()),
                () -> assertTrue(resultado.isActivo())
        );

        verify(repository).save(existente);
        verifyNoInteractions(productoClient);
    }

    @Test
    void updateDebeFallarCuandoInventarioNoExiste() {

        when(repository.findByProductoIdAndActivoTrue(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.update(
                        999L,
                        new InventarioUpdateRequest(
                                20,
                                4
                        )
                )
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void validarStockDebeRetornarTrueCuandoHayCantidadSuficiente() {

        when(repository.findByProductoIdAndActivoTrue(100L))
                .thenReturn(Optional.of(crearItem()));

        boolean resultado =
                service.validarStock(
                        100L,
                        5
                );

        assertTrue(resultado);
    }

    @Test
    void validarStockDebeRetornarTrueCuandoCantidadEsIgualAlStock() {

        when(repository.findByProductoIdAndActivoTrue(100L))
                .thenReturn(Optional.of(crearItem()));

        boolean resultado =
                service.validarStock(
                        100L,
                        10
                );

        assertTrue(resultado);
    }

    @Test
    void validarStockDebeRetornarFalseCuandoNoHayStockSuficiente() {

        when(repository.findByProductoIdAndActivoTrue(100L))
                .thenReturn(Optional.of(crearItem()));

        boolean resultado =
                service.validarStock(
                        100L,
                        11
                );

        assertFalse(resultado);
    }

    @Test
    void validarStockDebeRechazarCantidadCero() {

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.validarStock(
                                100L,
                                0
                        )
                );

        assertEquals(
                "La cantidad debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void validarStockDebeRechazarCantidadNula() {

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.validarStock(
                                100L,
                                null
                        )
                );

        assertEquals(
                "La cantidad debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void descontarStockDebeRestarCantidadCorrectamente() {

        InventarioItem item = crearItem();

        when(repository.findByProductoIdForUpdate(100L))
                .thenReturn(Optional.of(item));

        when(repository.save(any(InventarioItem.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        InventarioItem resultado =
                service.descontarStock(
                        100L,
                        4
                );

        assertEquals(
                6,
                resultado.getStockActual()
        );

        verify(repository).findByProductoIdForUpdate(100L);
        verify(repository).save(item);
    }

    @Test
    void descontarStockDebePermitirDejarStockEnCero() {

        InventarioItem item = crearItem();

        when(repository.findByProductoIdForUpdate(100L))
                .thenReturn(Optional.of(item));

        when(repository.save(any(InventarioItem.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        InventarioItem resultado =
                service.descontarStock(
                        100L,
                        10
                );

        assertEquals(
                0,
                resultado.getStockActual()
        );
    }

    @Test
    void descontarStockDebeRechazarStockInsuficiente() {

        InventarioItem item = crearItem();

        when(repository.findByProductoIdForUpdate(100L))
                .thenReturn(Optional.of(item));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.descontarStock(
                                100L,
                                20
                        )
                );

        assertEquals(
                "Stock insuficiente para producto 100",
                exception.getMessage()
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void descontarStockDebeFallarCuandoProductoNoTieneInventario() {

        when(repository.findByProductoIdForUpdate(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.descontarStock(
                                999L,
                                2
                        )
                );

        assertEquals(
                "Inventario no encontrado para producto: 999",
                exception.getMessage()
        );
    }

    @Test
    void descontarStockDebeRechazarCantidadNegativa() {

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.descontarStock(
                                100L,
                                -1
                        )
                );

        assertEquals(
                "La cantidad debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void reponerStockDebeSumarCantidadCorrectamente() {

        InventarioItem item = crearItem();

        when(repository.findByProductoIdForUpdate(100L))
                .thenReturn(Optional.of(item));

        when(repository.save(any(InventarioItem.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        InventarioItem resultado =
                service.reponerStock(
                        100L,
                        5
                );

        assertEquals(
                15,
                resultado.getStockActual()
        );

        verify(repository).save(item);
    }

    @Test
    void reponerStockDebeFallarCuandoInventarioNoExiste() {

        when(repository.findByProductoIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.reponerStock(
                        999L,
                        5
                )
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void bajoStockDebeUsarConsultaDelRepositorio() {

        InventarioItem item = crearItem();
        item.setStockActual(3);
        item.setStockMinimo(3);

        when(repository.findBajoStock())
                .thenReturn(List.of(item));

        List<InventarioItem> resultado =
                service.bajoStock();

        assertEquals(1, resultado.size());
        assertEquals(3, resultado.get(0).getStockActual());

        verify(repository).findBajoStock();
    }

    @Test
    void deleteDebeDesactivarInventario() {

        InventarioItem item = crearItem();

        when(repository.findByProductoIdAndActivoTrue(100L))
                .thenReturn(Optional.of(item));

        service.delete(100L);

        ArgumentCaptor<InventarioItem> captor =
                ArgumentCaptor.forClass(
                        InventarioItem.class
                );

        verify(repository).save(
                captor.capture()
        );

        assertFalse(
                captor.getValue().isActivo()
        );
    }

    @Test
    void deleteDebeFallarCuandoNoExiste() {

        when(repository.findByProductoIdAndActivoTrue(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(999L)
        );

        verify(repository, never())
                .save(any());
    }

    private void configurarProductoValido() {
        when(productoClient.findById(100L))
                .thenReturn(Map.of(
                        "id", 100L,
                        "activo", true
                ));
    }

    private InventarioRequest crearRequest() {
        return new InventarioRequest(
                100L,
                10,
                3
        );
    }

    private InventarioItem crearItem() {
        InventarioItem item =
                new InventarioItem();

        item.setId(1L);
        item.setProductoId(100L);
        item.setStockActual(10);
        item.setStockMinimo(3);
        item.setActivo(true);

        return item;
    }

    private FeignException crearFeign404(
            String url
    ) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                url,
                Collections
                        .<String, Collection<String>>
                                emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        Response response =
                Response.builder()
                        .status(404)
                        .reason("Not Found")
                        .request(request)
                        .headers(
                                Collections
                                        .<String, Collection<String>>
                                                emptyMap()
                        )
                        .build();

        return FeignException.errorStatus(
                "ProductoClient#findById",
                response
        );
    }
}