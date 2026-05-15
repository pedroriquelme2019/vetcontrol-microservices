CREATE TABLE IF NOT EXISTS clientes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rut VARCHAR(12) NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL,
  telefono VARCHAR(30) NOT NULL,
  correo VARCHAR(120) NOT NULL UNIQUE,
  direccion VARCHAR(180) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO clientes (rut,nombre,telefono,correo,direccion,activo) VALUES
('11111111-1','María González','+56911111111','maria.gonzalez@example.com','Av. Veterinaria 123',true),
('22222222-2','Carlos Pérez','+56922222222','carlos.perez@example.com','Los Aromos 456',true);
