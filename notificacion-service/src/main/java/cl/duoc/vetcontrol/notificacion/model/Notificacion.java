package cl.duoc.vetcontrol.notificacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 60
    )
    private TipoNotificacion tipo;

    @Column(
            nullable = false,
            length = 500
    )
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    private boolean leida = false;

    @Column(length = 100)
    private String origenEvento;

    private Long referenciaExternaId;

    @Column(
            length = 160,
            unique = true
    )
    private String claveEvento;

    public Notificacion() {
    }

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public String getOrigenEvento() {
        return origenEvento;
    }

    public void setOrigenEvento(String origenEvento) {
        this.origenEvento = origenEvento;
    }

    public Long getReferenciaExternaId() {
        return referenciaExternaId;
    }

    public void setReferenciaExternaId(
            Long referenciaExternaId
    ) {
        this.referenciaExternaId = referenciaExternaId;
    }

    public String getClaveEvento() {
        return claveEvento;
    }

    public void setClaveEvento(String claveEvento) {
        this.claveEvento = claveEvento;
    }
}