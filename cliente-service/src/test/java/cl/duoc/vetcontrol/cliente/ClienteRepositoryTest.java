package cl.duoc.vetcontrol.cliente;

import cl.duoc.vetcontrol.cliente.model.Cliente;
import cl.duoc.vetcontrol.cliente.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false"
})
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repository;

    @Test
    void repositoryDebeInyectarse() {
        assertNotNull(repository);
    }

    @Test
    void saveDebeGuardarCliente() {

        Cliente cliente = new Cliente();

        cliente.setRut("12345678-9");
        cliente.setNombre("Pedro");
        cliente.setTelefono("+56911111111");
        cliente.setCorreo("pedro@test.cl");
        cliente.setDireccion("Santiago");

        Cliente saved = repository.save(cliente);

        assertNotNull(saved.getId());
    }
}