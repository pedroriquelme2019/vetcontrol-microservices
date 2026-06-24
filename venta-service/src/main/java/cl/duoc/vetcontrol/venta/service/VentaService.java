package cl.duoc.vetcontrol.venta.service;

import cl.duoc.vetcontrol.venta.client.ClienteClient;
import cl.duoc.vetcontrol.venta.client.InventarioClient;
import cl.duoc.vetcontrol.venta.client.ProductoClient;
import cl.duoc.vetcontrol.venta.config.KafkaConfig;
import cl.duoc.vetcontrol.venta.dto.DetalleVentaRequest;
import cl.duoc.vetcontrol.venta.dto.ProductoDto;
import cl.duoc.vetcontrol.venta.dto.VentaRequest;
import cl.duoc.vetcontrol.venta.exception.BusinessException;
import cl.duoc.vetcontrol.venta.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.venta.model.DetalleVenta;
import cl.duoc.vetcontrol.venta.model.EstadoVenta;
import cl.duoc.vetcontrol.venta.model.Venta;
import cl.duoc.vetcontrol.venta.repository.VentaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class VentaService {

    private static final Logger log =
            LoggerFactory.getLogger(VentaService.class);

    private static final Set<String> MEDIOS_PAGO =
            Set.of(
                    "EFECTIVO",
                    "DEBITO",
                    "CREDITO",
                    "TRANSFERENCIA"
            );

    private final VentaRepository repository;
    private final ClienteClient clienteClient;
    private final ProductoClient productoClient;
    private final InventarioClient inventarioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public VentaService(
            VentaRepository repository,
            ClienteClient clienteClient,
            ProductoClient productoClient,
            InventarioClient inventarioClient,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.clienteClient = clienteClient;
        this.productoClient = productoClient;
        this.inventarioClient = inventarioClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<Venta> findAll() {
        return repository
                .findAllByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public Venta findById(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Venta no encontrada: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Venta> findByCliente(
            Long clienteId
    ) {
        return repository
                .findByClienteIdOrderByFechaDesc(
                        clienteId
                );
    }

    @Transactional
    public Venta create(VentaRequest request) {

        validarCliente(request.clienteId());

        String medioPago =
                normalizarMedioPago(
                        request.medioPago()
                );

        Map<Long, Integer> cantidades =
                agruparDetalles(
                        request.detalles()
                );

        Venta venta = new Venta();

        venta.setClienteId(
                request.clienteId()
        );

        venta.setMedioPago(
                medioPago
        );

        venta.setEstado(
                EstadoVenta.PENDIENTE
        );

        BigDecimal total =
                BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry
                : cantidades.entrySet()) {

            Long productoId =
                    entry.getKey();

            Integer cantidad =
                    entry.getValue();

            ProductoDto producto =
                    obtenerProducto(productoId);

            validarStock(
                    productoId,
                    cantidad
            );

            BigDecimal precio =
                    producto.precio()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal subtotal =
                    precio.multiply(
                                    BigDecimal.valueOf(
                                            cantidad
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            DetalleVenta detalle =
                    new DetalleVenta();

            detalle.setProductoId(
                    productoId
            );

            detalle.setCantidad(
                    cantidad
            );

            detalle.setPrecioUnitario(
                    precio
            );

            detalle.setSubtotal(
                    subtotal
            );

            venta.agregarDetalle(
                    detalle
            );

            total = total.add(
                    subtotal
            );
        }

        venta.setTotal(
                total.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        Venta pendiente =
                repository.save(venta);

        Map<Long, Integer> descontados =
                new LinkedHashMap<>();

        Venta registrada;

        try {
            for (DetalleVenta detalle
                    : pendiente.getDetalles()) {

                descontarStock(
                        detalle.getProductoId(),
                        detalle.getCantidad()
                );

                descontados.put(
                        detalle.getProductoId(),
                        detalle.getCantidad()
                );
            }

            pendiente.setEstado(
                    EstadoVenta.REGISTRADA
            );

            registrada =
                    repository.save(pendiente);

        } catch (RuntimeException exception) {

            compensarInventario(
                    descontados
            );

            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }

            throw new BusinessException(
                    "No fue posible completar la venta"
            );
        }

        publicarEventoVentaCreada(
                registrada
        );

        log.info(
                "Venta registrada id={} cliente={} total={}",
                registrada.getId(),
                registrada.getClienteId(),
                registrada.getTotal()
        );

        return registrada;
    }

    private void validarCliente(Long clienteId) {
        try {
            Map<String, Object> cliente =
                    clienteClient.findById(
                            clienteId
                    );

            if (cliente == null
                    || cliente.isEmpty()) {

                throw new BusinessException(
                        "Cliente no existe o no está disponible"
                );
            }

            Object activo =
                    cliente.get("activo");

            if (activo instanceof Boolean
                    && !((Boolean) activo)) {

                throw new BusinessException(
                        "Cliente no existe o no está disponible"
                );
            }

        } catch (FeignException exception) {
            throw new BusinessException(
                    "Cliente no existe o no está disponible"
            );
        }
    }

    private ProductoDto obtenerProducto(
            Long productoId
    ) {
        ProductoDto producto;

        try {
            producto =
                    productoClient.findById(
                            productoId
                    );

        } catch (FeignException exception) {
            throw new BusinessException(
                    "Producto no existe o no está disponible: "
                            + productoId
            );
        }

        if (producto == null
                || !producto.activo()) {

            throw new BusinessException(
                    "Producto no existe o no está disponible: "
                            + productoId
            );
        }

        if (producto.precio() == null
                || producto.precio()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "El producto tiene un precio inválido: "
                            + productoId
            );
        }

        return producto;
    }

    private void validarStock(
            Long productoId,
            Integer cantidad
    ) {
        try {
            Boolean disponible =
                    inventarioClient.validarStock(
                            productoId,
                            cantidad
                    );

            if (!Boolean.TRUE.equals(
                    disponible
            )) {
                throw new BusinessException(
                        "Stock insuficiente para producto "
                                + productoId
                );
            }

        } catch (FeignException exception) {
            throw new BusinessException(
                    "Inventario no disponible para producto "
                            + productoId
            );
        }
    }

    private void descontarStock(
            Long productoId,
            Integer cantidad
    ) {
        try {
            inventarioClient.descontarStock(
                    productoId,
                    cantidad
            );

        } catch (FeignException exception) {
            throw new BusinessException(
                    "No fue posible descontar stock del producto "
                            + productoId
            );
        }
    }

    private void compensarInventario(
            Map<Long, Integer> descontados
    ) {
        descontados.forEach(
                (productoId, cantidad) -> {
                    try {
                        inventarioClient.reponerStock(
                                productoId,
                                cantidad
                        );

                        log.warn(
                                "Stock compensado producto={} cantidad={}",
                                productoId,
                                cantidad
                        );

                    } catch (Exception exception) {
                        log.error(
                                "Error crítico compensando stock producto={} cantidad={}",
                                productoId,
                                cantidad,
                                exception
                        );
                    }
                }
        );
    }

    private Map<Long, Integer> agruparDetalles(
            List<DetalleVentaRequest> detalles
    ) {
        Map<Long, Integer> agrupados =
                new LinkedHashMap<>();

        for (DetalleVentaRequest detalle
                : detalles) {

            agrupados.merge(
                    detalle.productoId(),
                    detalle.cantidad(),
                    Integer::sum
            );
        }

        return agrupados;
    }

    private String normalizarMedioPago(
            String medioPago
    ) {
        String normalizado =
                medioPago.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!MEDIOS_PAGO.contains(
                normalizado
        )) {
            throw new BusinessException(
                    "Medio de pago no permitido"
            );
        }

        return normalizado;
    }

    private void publicarEventoVentaCreada(
            Venta venta
    ) {
        try {
            Map<String, Object> evento =
                    new LinkedHashMap<>();

            evento.put(
                    "tipo",
                    "VENTA_CREADA"
            );

            evento.put(
                    "ventaId",
                    venta.getId()
            );

            evento.put(
                    "clienteId",
                    venta.getClienteId()
            );

            evento.put(
                    "total",
                    venta.getTotal()
            );

            evento.put(
                    "medioPago",
                    venta.getMedioPago()
            );

            evento.put(
                    "estado",
                    venta.getEstado().name()
            );

            evento.put(
                    "fecha",
                    venta.getFecha()
            );

            String mensaje =
                    objectMapper.writeValueAsString(
                            evento
                    );

            kafkaTemplate.send(
                            KafkaConfig.TOPIC_VENTA_CREADA,
                            mensaje
                    )
                    .whenComplete(
                            (resultado, error) -> {

                                if (error != null) {
                                    log.error(
                                            "Kafka no pudo publicar la venta {}",
                                            venta.getId(),
                                            error
                                    );

                                    return;
                                }

                                log.info(
                                        "Evento Kafka publicado para venta {}",
                                        venta.getId()
                                );
                            }
                    );

        } catch (Exception exception) {
            log.error(
                    "No fue posible publicar el evento de la venta {}",
                    venta.getId(),
                    exception
            );
        }
    }
}