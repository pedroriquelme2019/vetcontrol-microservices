package cl.duoc.vetcontrol.veterinario.model;
import jakarta.persistence.*;
@Entity @Table(name="veterinarios")
public class Veterinario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, unique=true, length=12) private String rut;
 @Column(nullable=false, length=100) private String nombre;
 @Column(nullable=false, length=80) private String especialidad;
 @Column(nullable=false, unique=true, length=120) private String correo;
 @Column(nullable=false) private boolean activo=true;
 public Long getId(){return id;} public void setId(Long id){this.id=id;} public String getRut(){return rut;} public void setRut(String rut){this.rut=rut;} public String getNombre(){return nombre;} public void setNombre(String nombre){this.nombre=nombre;} public String getEspecialidad(){return especialidad;} public void setEspecialidad(String especialidad){this.especialidad=especialidad;} public String getCorreo(){return correo;} public void setCorreo(String correo){this.correo=correo;} public boolean isActivo(){return activo;} public void setActivo(boolean activo){this.activo=activo;}
}
