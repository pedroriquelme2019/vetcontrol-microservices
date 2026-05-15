package cl.duoc.vetcontrol.producto.service;
import cl.duoc.vetcontrol.producto.dto.ProductoRequest;
import cl.duoc.vetcontrol.producto.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.producto.model.Producto;
import cl.duoc.vetcontrol.producto.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class ProductoService { private final ProductoRepository repository; public ProductoService(ProductoRepository repository){this.repository=repository;} public List<Producto> findAll(){return repository.findAll();} public Producto findById(Long id){return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: "+id));} public Producto create(ProductoRequest r){return repository.save(map(new Producto(),r));} public Producto update(Long id,ProductoRequest r){return repository.save(map(findById(id),r));} public void delete(Long id){Producto p=findById(id);p.setActivo(false);repository.save(p);} public List<Producto> byCategoria(String c){return repository.findByCategoriaIgnoreCase(c);} private Producto map(Producto p,ProductoRequest r){p.setNombre(r.nombre());p.setCategoria(r.categoria());p.setPrecio(r.precio());p.setRestringido(r.restringido());return p;} }
