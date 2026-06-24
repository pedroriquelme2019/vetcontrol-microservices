package cl.duoc.vetcontrol.producto;

import cl.duoc.vetcontrol.producto.model.Producto;
import cl.duoc.vetcontrol.producto.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository repository;

    @Test
    void saveDebeGuardarProductoCompleto() {

        Producto producto =
                repository.saveAndFlush(
                        crearProducto(
                                "Vacuna",
                                "Medicamento",
                                true
                        )
                );

        assertAll(
                () -> assertNotNull(producto.getId()),
                () -> assertEquals(
                        "Vacuna",
                        producto.getNombre()
                ),
                () -> assertEquals(
                        "Medicamento",
                        producto.getCategoria()
                ),
                () -> assertNotNull(
                        producto.getPrecio()
                ),
                () -> assertTrue(
                        producto.isActivo()
                )
        );
    }

    @Test
    void findByActivoTrueDebeExcluirInactivos() {

        repository.saveAndFlush(
                crearProducto(
                        "Activo",
                        "Medicamento",
                        true
                )
        );

        repository.saveAndFlush(
                crearProducto(
                        "Inactivo",
                        "Medicamento",
                        false
                )
        );

        List<Producto> resultado =
                repository.findByActivoTrue();

        assertEquals(1, resultado.size());
        assertEquals(
                "Activo",
                resultado.get(0).getNombre()
        );
    }

    @Test
    void findByIdAndActivoTrueDebeEncontrarActivo() {

        Producto guardado =
                repository.saveAndFlush(
                        crearProducto(
                                "Vacuna",
                                "Medicamento",
                                true
                        )
                );

        Optional<Producto> resultado =
                repository.findByIdAndActivoTrue(
                        guardado.getId()
                );

        assertTrue(resultado.isPresent());
    }

    @Test
    void findByIdAndActivoTrueDebeIgnorarInactivo() {

        Producto guardado =
                repository.saveAndFlush(
                        crearProducto(
                                "Producto eliminado",
                                "Insumo",
                                false
                        )
                );

        Optional<Producto> resultado =
                repository.findByIdAndActivoTrue(
                        guardado.getId()
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByCategoriaDebeIgnorarMayusculas() {

        repository.saveAndFlush(
                crearProducto(
                        "Vacuna",
                        "Medicamento",
                        true
                )
        );

        repository.saveAndFlush(
                crearProducto(
                        "Collar",
                        "Accesorio",
                        true
                )
        );

        List<Producto> resultado =
                repository
                        .findByCategoriaIgnoreCaseAndActivoTrue(
                                "MEDICAMENTO"
                        );

        assertEquals(1, resultado.size());
        assertEquals(
                "Vacuna",
                resultado.get(0).getNombre()
        );
    }

    @Test
    void findByCategoriaDebeExcluirInactivos() {

        repository.saveAndFlush(
                crearProducto(
                        "Producto activo",
                        "Alimento",
                        true
                )
        );

        repository.saveAndFlush(
                crearProducto(
                        "Producto inactivo",
                        "Alimento",
                        false
                )
        );

        List<Producto> resultado =
                repository
                        .findByCategoriaIgnoreCaseAndActivoTrue(
                                "Alimento"
                        );

        assertEquals(1, resultado.size());
        assertEquals(
                "Producto activo",
                resultado.get(0).getNombre()
        );
    }

    @Test
    void searchDebeIgnorarMayusculasYExcluirInactivos() {

        repository.saveAndFlush(
                crearProducto(
                        "Antiparasitario",
                        "Medicamento",
                        true
                )
        );

        repository.saveAndFlush(
                crearProducto(
                        "Antiparasitario eliminado",
                        "Medicamento",
                        false
                )
        );

        List<Producto> resultado =
                repository
                        .findByNombreContainingIgnoreCaseAndActivoTrue(
                                "ANTI"
                        );

        assertEquals(1, resultado.size());
        assertEquals(
                "Antiparasitario",
                resultado.get(0).getNombre()
        );
    }

    private Producto crearProducto(
            String nombre,
            String categoria,
            boolean activo
    ) {
        Producto producto = new Producto();

        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(
                new BigDecimal("10000.00")
        );
        producto.setRestringido(false);
        producto.setActivo(activo);

        return producto;
    }
}