package cl.duoc.vetcontrol.mascota.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas")
public class Mascota {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long clienteId;
    @Column(nullable = false, length = 80) private String nombre;
    @Column(nullable = false, length = 30) private String especie;
    @Column(length = 60) private String raza;
    private Integer edad;
    @Column(length = 20) private String sexo;
    private Double peso;
    @Column(length = 50) private String microchip;
    @Column(nullable = false) private boolean activo = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getClienteId() { return clienteId; } public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecie() { return especie; } public void setEspecie(String especie) { this.especie = especie; }
    public String getRaza() { return raza; } public void setRaza(String raza) { this.raza = raza; }
    public Integer getEdad() { return edad; } public void setEdad(Integer edad) { this.edad = edad; }
    public String getSexo() { return sexo; } public void setSexo(String sexo) { this.sexo = sexo; }
    public Double getPeso() { return peso; } public void setPeso(Double peso) { this.peso = peso; }
    public String getMicrochip() { return microchip; } public void setMicrochip(String microchip) { this.microchip = microchip; }
    public boolean isActivo() { return activo; } public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
