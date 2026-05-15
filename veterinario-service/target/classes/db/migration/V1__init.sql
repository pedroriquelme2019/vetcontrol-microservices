CREATE TABLE IF NOT EXISTS veterinarios (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 rut VARCHAR(12) NOT NULL UNIQUE,
 nombre VARCHAR(100) NOT NULL,
 especialidad VARCHAR(80) NOT NULL,
 correo VARCHAR(120) NOT NULL UNIQUE,
 activo BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO veterinarios (rut,nombre,especialidad,correo,activo) VALUES
('33333333-3','Dra. Camila Rojas','Medicina interna','camila.rojas@vetcontrol.cl',true),
('44444444-4','Dr. Felipe Soto','Cirugía menor','felipe.soto@vetcontrol.cl',true);
