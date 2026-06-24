package cl.duoc.vetcontrol.inventario.service;

import cl.duoc.vetcontrol.inventario.client.ProductoClient;
import cl.duoc.vetcontrol.inventario.dto.InventarioRequest;
import cl.duoc.vetcontrol.inventario.dto.InventarioUpdateRequest;
import cl.duoc.vetcontrol.inventario.exception.BusinessException;
import cl.duoc.vetcontrol.inventario.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.inventario.model.InventarioItem;
import cl.duoc.vetcontrol.inventario.repository.InventarioRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InventarioService {

    private static final Logger log =
            LoggerFactory.getLogger(InventarioService.class);

    private final InventarioRepository repository;
    private final ProductoClient productoClient;

    public InventarioService(
            InventarioRepository repository,
            ProductoClient productoClient
    ) {
        this.repository = repository;
        this.productoClient = productoClient;
    }

    @Transactional(readOnly = true)
    public List<InventarioItem> findAll() {
        return repository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public InventarioItem findByProductoId(
            Long productoId
    ) {
        return repository
                .findByProductoIdAndActivoTrue(productoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventario no encontrado para producto: "
                                        + productoId
                        )
                );
    }

    @Transactional
    public InventarioItem create(
            InventarioRequest request
    ) {
        validarProducto(request.productoId());

        Optional<InventarioItem> existente =
                repository.findByProductoId(
                        request.productoId()
                );

        if (existente.isPresent()
                && existente.get().isActivo()) {

            throw new BusinessException(
                    "Ya existe inventario para el producto: "
                            + request.productoId()
            );
        }

        InventarioItem item =
                existente.orElseGet(InventarioItem::new);

        item.setProductoId(request.productoId());
        item.setStockActual(request.stockActual());
        item.setStockMinimo(request.stockMinimo());
        item.setActivo(true);

        InventarioItem guardado =
                repository.save(item);

        log.info(
                "Inventario creado o reactivado para producto={} stock={}",
                guardado.getProductoId(),
                guardado.getStockActual()
        );

        return guardado;
    }

    @Transactional
    public InventarioItem update(
            Long productoId,
            InventarioUpdateRequest request
    ) {
        InventarioItem item =
                findByProductoId(productoId);

        item.setStockActual(
                request.stockActual()
        );

        item.setStockMinimo(
                request.stockMinimo()
        );

        InventarioItem actualizado =
                repository.save(item);

        log.info(
                "Inventario actualizado producto={} stock={} mínimo={}",
                productoId,
                actualizado.getStockActual(),
                actualizado.getStockMinimo()
        );

        return actualizado;
    }

    @Transactional(readOnly = true)
    public boolean validarStock(
            Long productoId,
            Integer cantidad
    ) {
        validarCantidad(cantidad);

        InventarioItem item =
                findByProductoId(productoId);

        return item.getStockActual() >= cantidad;
    }

    @Transactional
    public InventarioItem descontarStock(
            Long productoId,
            Integer cantidad
    ) {
        validarCantidad(cantidad);

        InventarioItem item =
                obtenerParaActualizar(productoId);

        if (item.getStockActual() < cantidad) {
            throw new BusinessException(
                    "Stock insuficiente para producto "
                            + productoId
            );
        }

        item.setStockActual(
                item.getStockActual() - cantidad
        );

        InventarioItem actualizado =
                repository.save(item);

        log.info(
                "Stock descontado producto={} cantidad={} disponible={}",
                productoId,
                cantidad,
                actualizado.getStockActual()
        );

        return actualizado;
    }

    @Transactional
    public InventarioItem reponerStock(
            Long productoId,
            Integer cantidad
    ) {
        validarCantidad(cantidad);

        InventarioItem item =
                obtenerParaActualizar(productoId);

        item.setStockActual(
                item.getStockActual() + cantidad
        );

        InventarioItem actualizado =
                repository.save(item);

        log.info(
                "Stock repuesto producto={} cantidad={} disponible={}",
                productoId,
                cantidad,
                actualizado.getStockActual()
        );

        return actualizado;
    }

    @Transactional(readOnly = true)
    public List<InventarioItem> bajoStock() {
        return repository.findBajoStock();
    }

    @Transactional
    public void delete(Long productoId) {

        InventarioItem item =
                findByProductoId(productoId);

        item.setActivo(false);

        repository.save(item);

        log.info(
                "Inventario desactivado para producto={}",
                productoId
        );
    }

    private InventarioItem obtenerParaActualizar(
            Long productoId
    ) {
        return repository
                .findByProductoIdForUpdate(productoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventario no encontrado para producto: "
                                        + productoId
                        )
                );
    }

    private void validarCantidad(
            Integer cantidad
    ) {
        if (cantidad == null || cantidad <= 0) {
            throw new BusinessException(
                    "La cantidad debe ser mayor que cero"
            );
        }
    }

    private void validarProducto(
            Long productoId
    ) {
        try {
            Map<String, Object> producto =
                    productoClient.findById(productoId);

            if (producto == null || producto.isEmpty()) {
                throw new BusinessException(
                        "El producto no existe o no está disponible"
                );
            }

            Object activo = producto.get("activo");

            if (activo instanceof Boolean
                    && !((Boolean) activo)) {

                throw new BusinessException(
                        "El producto no existe o no está disponible"
                );
            }

        } catch (FeignException exception) {
            throw new BusinessException(
                    "El producto no existe o no está disponible"
            );
        }
    }
}