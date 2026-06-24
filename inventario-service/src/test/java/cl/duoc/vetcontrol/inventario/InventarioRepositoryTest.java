package cl.duoc.vetcontrol.inventario;

import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import cl.duoc.vetcontrol.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
class InventarioRepositoryTest {

    @Autowired
    private InventarioRepository repository;

    @Test
    void saveDebeGuardarInventarioCompleto() {

        InventarioItem guardado =
                repository.saveAndFlush(
                        crearItem(
                                100L,
                                10,
                                3,
                                true
                        )
                );

        assertAll(
                () -> assertNotNull(guardado.getId()),
                () -> assertEquals(100L, guardado.getProductoId()),
                () -> assertEquals(10, guardado.getStockActual()),
                () -> assertEquals(3, guardado.getStockMinimo()),
                () -> assertTrue(guardado.isActivo())
        );
    }

    @Test
    void findByActivoTrueDebeExcluirInactivos() {

        repository.saveAndFlush(
                crearItem(
                        100L,
                        10,
                        3,
                        true
                )
        );

        repository.saveAndFlush(
                crearItem(
                        200L,
                        20,
                        5,
                        false
                )
        );

        List<InventarioItem> resultado =
                repository.findByActivoTrue();

        assertEquals(1, resultado.size());
        assertEquals(
                100L,
                resultado.get(0).getProductoId()
        );
    }

    @Test
    void findByProductoIdDebeEncontrarRegistroInactivo() {

        repository.saveAndFlush(
                crearItem(
                        100L,
                        0,
                        3,
                        false
                )
        );

        Optional<InventarioItem> resultado =
                repository.findByProductoId(100L);

        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().isActivo());
    }

    @Test
    void findByProductoIdAndActivoTrueDebeIgnorarInactivo() {

        repository.saveAndFlush(
                crearItem(
                        100L,
                        0,
                        3,
                        false
                )
        );

        Optional<InventarioItem> resultado =
                repository
                        .findByProductoIdAndActivoTrue(
                                100L
                        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByProductoIdForUpdateDebeEncontrarActivo() {

        repository.saveAndFlush(
                crearItem(
                        100L,
                        10,
                        3,
                        true
                )
        );

        Optional<InventarioItem> resultado =
                repository
                        .findByProductoIdForUpdate(
                                100L
                        );

        assertTrue(resultado.isPresent());
        assertEquals(
                10,
                resultado.get().getStockActual()
        );
    }

    @Test
    void findByProductoIdForUpdateDebeIgnorarInactivo() {

        repository.saveAndFlush(
                crearItem(
                        100L,
                        10,
                        3,
                        false
                )
        );

        Optional<InventarioItem> resultado =
                repository
                        .findByProductoIdForUpdate(
                                100L
                        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findBajoStockDebeEncontrarStockMenorAlMinimo() {

        repository.saveAndFlush(
                crearItem(
                        100L,
                        2,
                        3,
                        true
                )
        );

        repository.saveAndFlush(
                crearItem(
                        200L,
                        20,
                        5,
                        true
                )
        );

        List<InventarioItem> resultado =
                repository.findBajoStock();

        assertEquals(1, resultado.size());
        assertEquals(
                100L,
                resultado.get(0).getProductoId()
        );
    }

    @Test
    void findBajoStockDebeIncluirStockIgualAlMinimo() {

        repository.saveAndFlush(
                crearItem(
                        100L,
                        3,
                        3,
                        true
                )
        );

        List<InventarioItem> resultado =
                repository.findBajoStock();

        assertEquals(1, resultado.size());
    }

    @Test
    void findBajoStockDebeIgnorarInventariosInactivos() {

        repository.saveAndFlush(
                crearItem(
                        100L,
                        0,
                        5,
                        false
                )
        );

        List<InventarioItem> resultado =
                repository.findBajoStock();

        assertTrue(resultado.isEmpty());
    }

    private InventarioItem crearItem(
            Long productoId,
            Integer stockActual,
            Integer stockMinimo,
            boolean activo
    ) {
        InventarioItem item =
                new InventarioItem();

        item.setProductoId(productoId);
        item.setStockActual(stockActual);
        item.setStockMinimo(stockMinimo);
        item.setActivo(activo);

        return item;
    }
}