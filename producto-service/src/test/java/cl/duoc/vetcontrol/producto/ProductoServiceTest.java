package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.dto.ProductoRequest;
import cl.duoc.vetcontrol.producto.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.producto.model.Producto;
import cl.duoc.vetcontrol.producto.repository.ProductoRepository;
import cl.duoc.vetcontrol.producto.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    private ProductoRepository repository;
    private ProductoService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProductoRepository.class);
        service = new ProductoService(repository);
    }

    @Test
    void findAllDebeRetornarSoloProductosActivos() {

        Producto producto = crearProducto();

        when(repository.findByActivoTrue())
                .thenReturn(List.of(producto));

        List<Producto> resultado =
                service.findAll();

        assertEquals(1, resultado.size());
        assertEquals(
                "Antiparasitario",
                resultado.get(0).getNombre()
        );

        verify(repository).findByActivoTrue();
    }

    @Test
    void findByIdDebeRetornarProductoActivo() {

        Producto producto = crearProducto();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(producto));

        Producto resultado =
                service.findById(1L);

        assertEquals(1L, resultado.getId());
        assertTrue(resultado.isActivo());
    }

    @Test
    void findByIdDebeLanzarExcepcionCuandoNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.findById(99L)
                );

        assertEquals(
                "Producto no encontrado: 99",
                exception.getMessage()
        );
    }

    @Test
    void byCategoriaDebeRetornarProductosActivos() {

        Producto producto = crearProducto();

        when(repository
                .findByCategoriaIgnoreCaseAndActivoTrue(
                        "Medicamento"
                ))
                .thenReturn(List.of(producto));

        List<Producto> resultado =
                service.byCategoria("Medicamento");

        assertEquals(1, resultado.size());
        assertEquals(
                "Medicamento",
                resultado.get(0).getCategoria()
        );
    }

    @Test
    void searchDebeBuscarPorNombre() {

        Producto producto = crearProducto();

        when(repository
                .findByNombreContainingIgnoreCaseAndActivoTrue(
                        "anti"
                ))
                .thenReturn(List.of(producto));

        List<Producto> resultado =
                service.search("anti");

        assertEquals(1, resultado.size());

        verify(repository)
                .findByNombreContainingIgnoreCaseAndActivoTrue(
                        "anti"
                );
    }

    @Test
    void createDebeGuardarTodosLosCampos() {

        ProductoRequest request = crearRequest(false);

        when(repository.save(any(Producto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Producto resultado =
                service.create(request);

        assertAll(
                () -> assertEquals(
                        "Antiparasitario",
                        resultado.getNombre()
                ),
                () -> assertEquals(
                        "Medicamento",
                        resultado.getCategoria()
                ),
                () -> assertEquals(
                        new BigDecimal("15000.00"),
                        resultado.getPrecio()
                ),
                () -> assertFalse(
                        resultado.isRestringido()
                ),
                () -> assertTrue(
                        resultado.isActivo()
                )
        );

        verify(repository).save(any(Producto.class));
    }

    @Test
    void createDebeGuardarProductoRestringido() {

        ProductoRequest request = crearRequest(true);

        when(repository.save(any(Producto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Producto resultado =
                service.create(request);

        assertTrue(resultado.isRestringido());
    }

    @Test
    void updateDebeActualizarTodosLosCampos() {

        Producto producto = crearProducto();

        ProductoRequest request =
                new ProductoRequest(
                        "Anestesia veterinaria",
                        "Insumo clínico",
                        new BigDecimal("50000.00"),
                        true
                );

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(producto));

        when(repository.save(any(Producto.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Producto resultado =
                service.update(1L, request);

        assertAll(
                () -> assertEquals(
                        1L,
                        resultado.getId()
                ),
                () -> assertEquals(
                        "Anestesia veterinaria",
                        resultado.getNombre()
                ),
                () -> assertEquals(
                        "Insumo clínico",
                        resultado.getCategoria()
                ),
                () -> assertEquals(
                        new BigDecimal("50000.00"),
                        resultado.getPrecio()
                ),
                () -> assertTrue(
                        resultado.isRestringido()
                ),
                () -> assertTrue(
                        resultado.isActivo()
                )
        );
    }

    @Test
    void updateDebeFallarCuandoProductoNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.update(
                        99L,
                        crearRequest(false)
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deleteDebeRealizarEliminacionLogica() {

        Producto producto = crearProducto();

        when(repository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(producto));

        service.delete(1L);

        ArgumentCaptor<Producto> captor =
                ArgumentCaptor.forClass(Producto.class);

        verify(repository).save(captor.capture());

        assertFalse(
                captor.getValue().isActivo()
        );
    }

    @Test
    void deleteDebeFallarCuandoProductoNoExiste() {

        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(99L)
        );

        verify(repository, never()).save(any());
    }

    private ProductoRequest crearRequest(
            boolean restringido
    ) {
        return new ProductoRequest(
                "Antiparasitario",
                "Medicamento",
                new BigDecimal("15000.00"),
                restringido
        );
    }

    private Producto crearProducto() {

        Producto producto = new Producto();

        producto.setId(1L);
        producto.setNombre("Antiparasitario");
        producto.setCategoria("Medicamento");
        producto.setPrecio(
                new BigDecimal("15000.00")
        );
        producto.setRestringido(false);
        producto.setActivo(true);

        return producto;
    }
}