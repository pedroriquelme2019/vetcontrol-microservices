package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.repository.ClienteRepository;
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
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repository;

    @Test
    void saveDebeGuardarClienteCompleto() {

        Cliente cliente = crearCliente(
                "11111111-1",
                "Joaquín González",
                "joaquin@correo.cl",
                true
        );

        Cliente guardado =
                repository.saveAndFlush(cliente);

        assertAll(
                () -> assertNotNull(guardado.getId()),
                () -> assertEquals(
                        "11111111-1",
                        guardado.getRut()
                ),
                () -> assertEquals(
                        "Joaquín González",
                        guardado.getNombre()
                ),
                () -> assertNotNull(
                        guardado.getCreatedAt()
                ),
                () -> assertTrue(
                        guardado.isActivo()
                )
        );
    }

    @Test
    void findByActivoTrueDebeExcluirInactivos() {

        repository.saveAndFlush(
                crearCliente(
                        "22222222-2",
                        "Cliente Activo",
                        "activo@correo.cl",
                        true
                )
        );

        repository.saveAndFlush(
                crearCliente(
                        "33333333-3",
                        "Cliente Inactivo",
                        "inactivo@correo.cl",
                        false
                )
        );

        List<Cliente> resultado =
                repository.findByActivoTrue();

        assertEquals(1, resultado.size());
        assertEquals(
                "Cliente Activo",
                resultado.get(0).getNombre()
        );
    }

    @Test
    void findByIdAndActivoTrueDebeEncontrarActivo() {

        Cliente guardado =
                repository.saveAndFlush(
                        crearCliente(
                                "44444444-4",
                                "Ana Torres",
                                "ana@correo.cl",
                                true
                        )
                );

        Optional<Cliente> resultado =
                repository.findByIdAndActivoTrue(
                        guardado.getId()
                );

        assertTrue(resultado.isPresent());
        assertEquals(
                "Ana Torres",
                resultado.get().getNombre()
        );
    }

    @Test
    void findByIdAndActivoTrueDebeIgnorarInactivo() {

        Cliente guardado =
                repository.saveAndFlush(
                        crearCliente(
                                "55555555-5",
                                "Pedro Inactivo",
                                "pedro@correo.cl",
                                false
                        )
                );

        Optional<Cliente> resultado =
                repository.findByIdAndActivoTrue(
                        guardado.getId()
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void searchDebeIgnorarMayusculasYExcluirInactivos() {

        repository.saveAndFlush(
                crearCliente(
                        "66666666-6",
                        "Ana María",
                        "ana.maria@correo.cl",
                        true
                )
        );

        repository.saveAndFlush(
                crearCliente(
                        "77777777-7",
                        "Ana Inactiva",
                        "ana.inactiva@correo.cl",
                        false
                )
        );

        repository.saveAndFlush(
                crearCliente(
                        "88888888-8",
                        "Carlos Soto",
                        "carlos@correo.cl",
                        true
                )
        );

        List<Cliente> resultado =
                repository
                        .findByNombreContainingIgnoreCaseAndActivoTrue(
                                "ANA"
                        );

        assertEquals(1, resultado.size());
        assertEquals(
                "Ana María",
                resultado.get(0).getNombre()
        );
    }

    @Test
    void existsByRutDebeDetectarExistencia() {

        repository.saveAndFlush(
                crearCliente(
                        "99999999-9",
                        "Cliente RUT",
                        "rut@correo.cl",
                        true
                )
        );

        assertTrue(
                repository.existsByRut(
                        "99999999-9"
                )
        );

        assertFalse(
                repository.existsByRut(
                        "10101010-1"
                )
        );
    }

    @Test
    void existsByCorreoDebeDetectarExistencia() {

        repository.saveAndFlush(
                crearCliente(
                        "10101010-1",
                        "Cliente Correo",
                        "existe@correo.cl",
                        true
                )
        );

        assertTrue(
                repository.existsByCorreo(
                        "existe@correo.cl"
                )
        );

        assertFalse(
                repository.existsByCorreo(
                        "noexiste@correo.cl"
                )
        );
    }

    @Test
    void existsByRutAndIdNotDebeIgnorarMismoCliente() {

        Cliente guardado =
                repository.saveAndFlush(
                        crearCliente(
                                "12121212-1",
                                "Cliente Uno",
                                "uno@correo.cl",
                                true
                        )
                );

        assertFalse(
                repository.existsByRutAndIdNot(
                        "12121212-1",
                        guardado.getId()
                )
        );

        assertTrue(
                repository.existsByRutAndIdNot(
                        "12121212-1",
                        999L
                )
        );
    }

    @Test
    void existsByCorreoAndIdNotDebeIgnorarMismoCliente() {

        Cliente guardado =
                repository.saveAndFlush(
                        crearCliente(
                                "13131313-1",
                                "Cliente Dos",
                                "dos@correo.cl",
                                true
                        )
                );

        assertFalse(
                repository.existsByCorreoAndIdNot(
                        "dos@correo.cl",
                        guardado.getId()
                )
        );

        assertTrue(
                repository.existsByCorreoAndIdNot(
                        "dos@correo.cl",
                        999L
                )
        );
    }

    private Cliente crearCliente(
            String rut,
            String nombre,
            String correo,
            boolean activo
    ) {
        Cliente cliente = new Cliente();

        cliente.setRut(rut);
        cliente.setNombre(nombre);
        cliente.setTelefono("+56999999999");
        cliente.setCorreo(correo);
        cliente.setDireccion("Santiago");
        cliente.setActivo(activo);

        return cliente;
    }
}