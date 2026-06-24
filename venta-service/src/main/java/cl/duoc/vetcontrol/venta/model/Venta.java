package cl.duoc.vetcontrol.venta.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private LocalDateTime fecha =
            LocalDateTime.now();

    @Column(
            nullable = false,
            length = 30
    )
    private String medioPago;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal total =
            BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private EstadoVenta estado =
            EstadoVenta.PENDIENTE;

    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<DetalleVenta> detalles =
            new ArrayList<>();

    public Venta() {
    }

    public void agregarDetalle(
            DetalleVenta detalle
    ) {
        detalle.setVenta(this);
        detalles.add(detalle);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoVenta estado) {
        this.estado = estado;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(
            List<DetalleVenta> detalles
    ) {
        this.detalles.clear();

        if (detalles != null) {
            detalles.forEach(this::agregarDetalle);
        }
    }
}