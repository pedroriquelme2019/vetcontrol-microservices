package cl.duoc.vetcontrol.veterinario;

import cl.duoc.vetcontrol.veterinario.dto.VeterinarioRequest;
import cl.duoc.vetcontrol.veterinario.exception.BusinessException;
import cl.duoc.vetcontrol.veterinario.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.veterinario.model.Veterinario;
import cl.duoc.vetcontrol.veterinario.repository.VeterinarioRepository;
import cl.duoc.vetcontrol.veterinario.service.VeterinarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VeterinarioServiceTest {

    private VeterinarioRepository repository;
    private VeterinarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(VeterinarioRepository.class);
        service = new VeterinarioService(repository);
    }

    @Test
    void findAllDebeRetornarVeterinarios() {

        Veterinario veterinario = new Veterinario();
        veterinario.setId(1L);
        veterinario.setNombre("Juan Perez");

        when(repository.findAll())
                .thenReturn(List.of(veterinario));

        List<Veterinario> resultado = service.findAll();

        assertEquals(1, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
    }

    @Test
    void findByIdDebeRetornarVeterinario() {

        Veterinario veterinario = new Veterinario();
        veterinario.setId(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(veterinario));

        Veterinario resultado = service.findById(1L);

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
    void createDebeGuardarVeterinario() {

        VeterinarioRequest request = new VeterinarioRequest(
                "11111111-1",
                "Juan Perez",
                "Cirugia",
                "juan@correo.cl"
        );

        when(repository.existsByRut(request.rut()))
                .thenReturn(false);

        when(repository.existsByCorreo(request.correo()))
                .thenReturn(false);

        when(repository.save(any(Veterinario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veterinario resultado = service.create(request);

        assertEquals("Juan Perez", resultado.getNombre());
        assertEquals("Cirugia", resultado.getEspecialidad());

        verify(repository).save(any(Veterinario.class));
    }

    @Test
    void createDebeLanzarExcepcionPorRutDuplicado() {

        VeterinarioRequest request = new VeterinarioRequest(
                "11111111-1",
                "Juan Perez",
                "Cirugia",
                "juan@correo.cl"
        );

        when(repository.existsByRut(request.rut()))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );
    }

    @Test
    void createDebeLanzarExcepcionPorCorreoDuplicado() {

        VeterinarioRequest request = new VeterinarioRequest(
                "11111111-1",
                "Juan Perez",
                "Cirugia",
                "juan@correo.cl"
        );

        when(repository.existsByRut(request.rut()))
                .thenReturn(false);

        when(repository.existsByCorreo(request.correo()))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );
    }

    @Test
    void updateDebeModificarVeterinario() {

        Veterinario veterinario = new Veterinario();
        veterinario.setId(1L);
        veterinario.setNombre("Nombre Viejo");

        VeterinarioRequest request = new VeterinarioRequest(
                "11111111-1",
                "Nombre Nuevo",
                "Dermatologia",
                "nuevo@correo.cl"
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(veterinario));

        when(repository.save(any(Veterinario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veterinario resultado = service.update(1L, request);

        assertEquals("Nombre Nuevo", resultado.getNombre());
        assertEquals("Dermatologia", resultado.getEspecialidad());
    }

    @Test
    void deleteDebeDesactivarVeterinario() {

        Veterinario veterinario = new Veterinario();
        veterinario.setId(1L);
        veterinario.setActivo(true);

        when(repository.findById(1L))
                .thenReturn(Optional.of(veterinario));

        service.delete(1L);

        assertFalse(veterinario.isActivo());

        verify(repository).save(veterinario);
    }

    @Test
    void byEspecialidadDebeRetornarVeterinarios() {

        Veterinario veterinario = new Veterinario();
        veterinario.setEspecialidad("Cirugia");

        when(repository.findByEspecialidadContainingIgnoreCase("Cirugia"))
                .thenReturn(List.of(veterinario));

        List<Veterinario> resultado = service.byEspecialidad("Cirugia");

        assertEquals(1, resultado.size());
        assertEquals("Cirugia", resultado.get(0).getEspecialidad());
    }
}