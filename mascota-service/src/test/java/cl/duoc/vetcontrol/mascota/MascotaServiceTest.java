package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.client.ClienteClient;
import cl.duoc.vetcontrol.mascota.dto.MascotaRequest;
import cl.duoc.vetcontrol.mascota.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.mascota.model.Mascota;
import cl.duoc.vetcontrol.mascota.repository.MascotaRepository;
import cl.duoc.vetcontrol.mascota.service.MascotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MascotaServiceTest {

    private MascotaRepository repository;
    private ClienteClient clienteClient;
    private MascotaService service;

    @BeforeEach
    void setUp() {
        repository = mock(MascotaRepository.class);
        clienteClient = mock(ClienteClient.class);
        service = new MascotaService(repository, clienteClient);
    }

    @Test
    void findAllDebeRetornarMascotas() {

        Mascota mascota = new Mascota();
        mascota.setId(1L);
        mascota.setNombre("Firulais");

        when(repository.findAll()).thenReturn(List.of(mascota));

        List<Mascota> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals("Firulais", resultado.get(0).getNombre());
    }

    @Test
    void findByIdDebeRetornarMascota() {

        Mascota mascota = new Mascota();
        mascota.setId(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(mascota));

        Mascota resultado = service.findById(1L);

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
    void findByClienteDebeRetornarMascotas() {

        Mascota mascota = new Mascota();
        mascota.setClienteId(10L);

        when(repository.findByClienteId(10L))
                .thenReturn(List.of(mascota));

        List<Mascota> resultado = service.findByCliente(10L);

        assertEquals(1, resultado.size());
    }

    @Test
    void createDebeGuardarMascota() {

        MascotaRequest request = new MascotaRequest(
                1L,
                "Firulais",
                "Perro",
                "Labrador",
                3,
                "Macho",
                20.0,
                "MC123"
        );

        when(clienteClient.findById(1L))
                .thenReturn(Map.of("id", 1));

        when(repository.save(any(Mascota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mascota resultado = service.create(request);

        assertEquals("Firulais", resultado.getNombre());

        verify(repository).save(any(Mascota.class));
    }


    @Test
    void updateDebeModificarMascota() {

        Mascota mascota = new Mascota();
        mascota.setId(1L);
        mascota.setNombre("Nombre Viejo");

        MascotaRequest request = new MascotaRequest(
                1L,
                "Nombre Nuevo",
                "Perro",
                "Labrador",
                4,
                "Macho",
                22.0,
                "MC456"
        );

        when(clienteClient.findById(1L))
                .thenReturn(Map.of("id", 1));

        when(repository.findById(1L))
                .thenReturn(Optional.of(mascota));

        when(repository.save(any(Mascota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mascota resultado = service.update(1L, request);

        assertEquals("Nombre Nuevo", resultado.getNombre());
    }

    @Test
    void deleteDebeDesactivarMascota() {

        Mascota mascota = new Mascota();
        mascota.setId(1L);
        mascota.setActivo(true);

        when(repository.findById(1L))
                .thenReturn(Optional.of(mascota));

        service.delete(1L);

        assertFalse(mascota.isActivo());

        verify(repository).save(mascota);
    }
}