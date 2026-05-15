CREATE TABLE IF NOT EXISTS atenciones (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 cita_id BIGINT NOT NULL,
 mascota_id BIGINT NOT NULL,
 veterinario_id BIGINT NOT NULL,
 fecha_atencion DATETIME NOT NULL,
 diagnostico VARCHAR(300) NOT NULL,
 tratamiento VARCHAR(300) NOT NULL,
 observaciones VARCHAR(500)
);
