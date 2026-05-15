CREATE TABLE IF NOT EXISTS citas (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 mascota_id BIGINT NOT NULL,
 veterinario_id BIGINT NOT NULL,
 fecha DATE NOT NULL,
 hora TIME NOT NULL,
 motivo VARCHAR(160) NOT NULL,
 estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA',
 UNIQUE KEY uk_vet_fecha_hora (veterinario_id, fecha, hora)
);
