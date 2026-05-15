CREATE TABLE IF NOT EXISTS inventario (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 producto_id BIGINT NOT NULL UNIQUE,
 stock_actual INT NOT NULL,
 stock_minimo INT NOT NULL
);
INSERT INTO inventario (producto_id,stock_actual,stock_minimo) VALUES (1,25,5),(2,12,4),(3,20,3);
