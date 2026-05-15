package cl.duoc.vetcontrol.inventario.service;
import cl.duoc.vetcontrol.inventario.dto.InventarioRequest;
import cl.duoc.vetcontrol.inventario.exception.BusinessException;
import cl.duoc.vetcontrol.inventario.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import cl.duoc.vetcontrol.inventario.repository.InventarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class InventarioService { private final InventarioRepository repository; public InventarioService(InventarioRepository repository){this.repository=repository;} public List<InventarioItem> findAll(){return repository.findAll();} public InventarioItem findByProductoId(Long productoId){return repository.findByProductoId(productoId).orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para producto: "+productoId));} public InventarioItem create(InventarioRequest r){InventarioItem i=new InventarioItem(); i.setProductoId(r.productoId()); i.setStockActual(r.stockActual()); i.setStockMinimo(r.stockMinimo()); return repository.save(i);} public boolean validarStock(Long productoId,Integer cantidad){return findByProductoId(productoId).getStockActual() >= cantidad;} public void descontarStock(Long productoId,Integer cantidad){InventarioItem i=findByProductoId(productoId); if(i.getStockActual()<cantidad) throw new BusinessException("Stock insuficiente para producto "+productoId); i.setStockActual(i.getStockActual()-cantidad); repository.save(i);} public List<InventarioItem> bajoStock(){return repository.findAll().stream().filter(i -> i.getStockActual() <= i.getStockMinimo()).toList();} }
