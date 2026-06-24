package cl.duoc.vetcontrol.producto.service;

import cl.duoc.vetcontrol.producto.dto.ProductoRequest;
import cl.duoc.vetcontrol.producto.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.producto.model.Producto;
import cl.duoc.vetcontrol.producto.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private static final Logger log =
            LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> findAll() {
        return repository.findByActivoTrue();
    }

    public Producto findById(Long id) {
        return repository
                .findByIdAndActivoTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Producto no encontrado: " + id
                        )
                );
    }

    public List<Producto> byCategoria(String categoria) {
        return repository
                .findByCategoriaIgnoreCaseAndActivoTrue(
                        categoria
                );
    }

    public List<Producto> search(String nombre) {
        return repository
                .findByNombreContainingIgnoreCaseAndActivoTrue(
                        nombre
                );
    }

    public Producto create(ProductoRequest request) {

        Producto producto = map(
                new Producto(),
                request
        );

        log.info(
                "Creando producto {}",
                producto.getNombre()
        );

        return repository.save(producto);
    }

    public Producto update(
            Long id,
            ProductoRequest request
    ) {
        Producto producto = findById(id);

        map(producto, request);

        log.info(
                "Actualizando producto {}",
                id
        );

        return repository.save(producto);
    }

    public void delete(Long id) {

        Producto producto = findById(id);

        producto.setActivo(false);

        repository.save(producto);

        log.info(
                "Producto {} marcado como inactivo",
                id
        );
    }

    private Producto map(
            Producto producto,
            ProductoRequest request
    ) {
        producto.setNombre(request.nombre());
        producto.setCategoria(request.categoria());
        producto.setPrecio(request.precio());
        producto.setRestringido(request.restringido());

        return producto;
    }
}