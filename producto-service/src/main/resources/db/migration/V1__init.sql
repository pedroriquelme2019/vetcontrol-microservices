CREATE TABLE IF NOT EXISTS productos (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 nombre VARCHAR(120) NOT NULL,
 categoria VARCHAR(50) NOT NULL,
 precio DECIMAL(10,2) NOT NULL,
 restringido BOOLEAN NOT NULL DEFAULT FALSE,
 activo BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO productos (nombre,categoria,precio,restringido,activo) VALUES
('Alimento Premium Perro 10kg','Alimentos',28990,false,true),
('Antiparasitario interno','Medicamentos',8990,true,true),
('Shampoo hipoalergénico','Higiene',6990,false,true);
