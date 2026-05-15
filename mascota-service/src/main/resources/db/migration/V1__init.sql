CREATE TABLE IF NOT EXISTS mascotas (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 cliente_id BIGINT NOT NULL,
 nombre VARCHAR(80) NOT NULL,
 especie VARCHAR(30) NOT NULL,
 raza VARCHAR(60),
 edad INT,
 sexo VARCHAR(20),
 peso DOUBLE,
 microchip VARCHAR(50),
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO mascotas (cliente_id,nombre,especie,raza,edad,sexo,peso,microchip,activo) VALUES
(1,'Luna','Perro','Poodle',4,'Hembra',7.5,'CHIP-001',true),
(2,'Milo','Gato','Doméstico',2,'Macho',4.1,'CHIP-002',true);
