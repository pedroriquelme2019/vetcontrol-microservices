package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.dto.ProductoRequest;
import cl.duoc.vetcontrol.producto.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.producto.model.Producto;
import cl.duoc.vetcontrol.producto.repository.ProductoRepository;
import cl.duoc.vetcontrol.producto.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void findAllDebeRetornarProductos() {

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Vacuna");

        when(repository.findAll())
                .thenReturn(List.of(producto));

        List<Producto> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals("Vacuna", resultado.get(0).getNombre());
    }

    @Test
    void findByIdDebeRetornarProducto() {

        Producto producto = new Producto();
        producto.setId(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        Producto resultado = service.findById(1L);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdDebeLanzarExcepcion() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(1L)
        );
    }

    @Test
    void createDebeGuardarProducto() {

        ProductoRequest request = new ProductoRequest(
                "Antiparasitario",
                "Medicamento",
                new BigDecimal("15000"),
                false
        );

        when(repository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = service.create(request);

        assertEquals("Antiparasitario", resultado.getNombre());
        assertEquals("Medicamento", resultado.getCategoria());

        verify(repository).save(any(Producto.class));
    }

    @Test
    void updateDebeModificarProducto() {

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Viejo");

        ProductoRequest request = new ProductoRequest(
                "Nuevo",
                "Medicamento",
                new BigDecimal("20000"),
                false
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(repository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = service.update(1L, request);

        assertEquals("Nuevo", resultado.getNombre());
        assertEquals(new BigDecimal("20000"), resultado.getPrecio());
    }

    @Test
    void deleteDebeDesactivarProducto() {

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setActivo(true);

        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        service.delete(1L);

        assertFalse(producto.isActivo());

        verify(repository).save(producto);
    }

    @Test
    void byCategoriaDebeRetornarProductos() {

        Producto producto = new Producto();
        producto.setCategoria("Medicamento");

        when(repository.findByCategoriaIgnoreCase("Medicamento"))
                .thenReturn(List.of(producto));

        List<Producto> resultado = service.byCategoria("Medicamento");

        assertEquals(1, resultado.size());
    }

    @Test
    void createDebeGuardarProductoRestringido() {

        ProductoRequest request = new ProductoRequest(
                "Anestesia",
                "Medicamento",
                new BigDecimal("50000"),
                true
        );

        when(repository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = service.create(request);

        assertTrue(resultado.isRestringido());
    }
}