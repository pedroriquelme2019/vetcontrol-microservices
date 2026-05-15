package cl.duoc.vetcontrol.venta.service;

import cl.duoc.vetcontrol.venta.client.ClienteClient;
import cl.duoc.vetcontrol.venta.client.InventarioClient;
import cl.duoc.vetcontrol.venta.client.ProductoClient;
import cl.duoc.vetcontrol.venta.dto.DetalleVentaRequest;
import cl.duoc.vetcontrol.venta.dto.ProductoDto;
import cl.duoc.vetcontrol.venta.dto.VentaRequest;
import cl.duoc.vetcontrol.venta.exception.BusinessException;
import cl.duoc.vetcontrol.venta.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.venta.model.DetalleVenta;
import cl.duoc.vetcontrol.venta.model.Venta;
import cl.duoc.vetcontrol.venta.repository.VentaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final VentaRepository repository;
    private final ClienteClient clienteClient;
    private final ProductoClient productoClient;
    private final InventarioClient inventarioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public VentaService(VentaRepository repository,
                        ClienteClient clienteClient,
                        ProductoClient productoClient,
                        InventarioClient inventarioClient,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.clienteClient = clienteClient;
        this.productoClient = productoClient;
        this.inventarioClient = inventarioClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Venta> all() {
        return repository.findAll();
    }

    public Venta one(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + id));
    }

    public Venta create(VentaRequest request) {
        validarCliente(request.clienteId());

        Venta venta = new Venta();
        venta.setClienteId(request.clienteId());
        venta.setMedioPago(request.medioPago());

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequest item : request.detalles()) {
            ProductoDto producto = productoClient.findById(item.productoId());
            Boolean stockDisponible = inventarioClient.validarStock(item.productoId(), item.cantidad());

            if (!Boolean.TRUE.equals(stockDisponible)) {
                throw new BusinessException("Stock insuficiente para producto " + item.productoId());
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProductoId(item.productoId());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.precio());
            detalle.setSubtotal(producto.precio().multiply(BigDecimal.valueOf(item.cantidad())));

            venta.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
        }

        venta.setTotal(total);
        Venta saved = repository.save(venta);

        for (DetalleVentaRequest item : request.detalles()) {
            inventarioClient.descontarStock(item.productoId(), item.cantidad());
        }

        String evento = "{\"ventaId\":" + saved.getId()
                + ",\"clienteId\":" + saved.getClienteId()
                + ",\"total\":" + saved.getTotal() + "}";

        kafkaTemplate.send("venta-creada", evento);

        log.info("Venta registrada id={} total={}", saved.getId(), saved.getTotal());

        return saved;
    }

    public List<Venta> byCliente(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    private void validarCliente(Long id) {
        try {
            clienteClient.findById(id);
        } catch (FeignException ex) {
            throw new BusinessException("Cliente no existe o no disponible");
        }
    }
}