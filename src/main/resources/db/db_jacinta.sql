-- =====================================================================
-- Vestuarios Jacinta · EP3 DSY1104 · Modelo relacional
-- Tablas: USUARIO -> BOLETA -> DETALLE_BOLETA <- PRODUCTO <- CATEGORIA
-- =====================================================================

CREATE DATABASE IF NOT EXISTS jacinta_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE jacinta_db;

DROP TABLE IF EXISTS detalle_boleta;
DROP TABLE IF EXISTS boleta;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS usuario;

CREATE TABLE usuario (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  run              VARCHAR(9)   NOT NULL, -- normalizado: sin puntos ni guion
  nombre           VARCHAR(50)  NOT NULL,
  apellidos        VARCHAR(100) NOT NULL,
  correo           VARCHAR(100) NOT NULL,
  contrasena       VARCHAR(60)  NOT NULL,
  fecha_nacimiento DATE         NULL,
  tipo_usuario     ENUM('ADMINISTRADOR','VENDEDOR','CLIENTE') NOT NULL DEFAULT 'CLIENTE',
  region           VARCHAR(100) NULL,
  comuna           VARCHAR(100) NULL,
  direccion        VARCHAR(300) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuario_run    (run),
  UNIQUE KEY uk_usuario_correo (correo)
) ENGINE=InnoDB;

CREATE TABLE categoria (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(100) NOT NULL,
  descripcion VARCHAR(500) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categoria_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE producto (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  nombre        VARCHAR(150)  NOT NULL,
  genero        VARCHAR(20)   NOT NULL,
  tipo          VARCHAR(50)   NOT NULL,
  categoria_id  BIGINT        NOT NULL,
  precio        INT           NOT NULL,
  stock         INT           NOT NULL DEFAULT 0,
  stock_critico INT           NOT NULL DEFAULT 5,
  descripcion   VARCHAR(500)  NULL,
  tallas        JSON          NOT NULL,
  colores       JSON          NOT NULL,
  activo        BOOLEAN       NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_producto_genero_tipo (genero, tipo),
  CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id)
    REFERENCES categoria (id) ON DELETE RESTRICT,
  CONSTRAINT ck_producto_precio CHECK (precio >= 0),
  CONSTRAINT ck_producto_stock  CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE boleta (
  id                   BIGINT       NOT NULL AUTO_INCREMENT,
  numero               VARCHAR(20)  NOT NULL,
  fecha                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado               ENUM('PAGADA','PAGO_FALLIDO','PENDIENTE') NOT NULL DEFAULT 'PENDIENTE',
  stock_reservado      BOOLEAN      NOT NULL DEFAULT FALSE,
  usuario_id           BIGINT       NULL,
  cliente_nombre       VARCHAR(50)  NOT NULL,
  cliente_apellidos    VARCHAR(100) NOT NULL,
  cliente_correo       VARCHAR(100) NOT NULL,
  entrega_calle        VARCHAR(300) NOT NULL,
  entrega_departamento VARCHAR(50)  NULL,
  entrega_region       VARCHAR(100) NOT NULL,
  entrega_comuna       VARCHAR(100) NOT NULL,
  entrega_indicaciones VARCHAR(500) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_boleta_numero (numero),
  KEY idx_boleta_usuario (usuario_id),
  CONSTRAINT fk_boleta_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE detalle_boleta (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  boleta_id       BIGINT       NOT NULL,
  producto_id     BIGINT       NOT NULL,
  nombre_producto VARCHAR(150) NOT NULL,
  precio_unitario INT          NOT NULL,
  cantidad        INT          NOT NULL,
  talla           VARCHAR(10)  NULL,
  color           VARCHAR(50)  NULL,
  PRIMARY KEY (id),
  KEY idx_detalle_boleta  (boleta_id),
  KEY idx_detalle_producto (producto_id),
  CONSTRAINT fk_detalle_boleta   FOREIGN KEY (boleta_id)
    REFERENCES boleta (id)   ON DELETE CASCADE,
  CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id)
    REFERENCES producto (id) ON DELETE RESTRICT,
  CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_precio   CHECK (precio_unitario >= 0)
) ENGINE=InnoDB;


-- DATOS INICIALES

-- contraseña 1234
INSERT INTO usuario (run, nombre, apellidos, correo, contrasena, tipo_usuario, region, comuna, direccion)
VALUES
('190110222', 'Admin', 'Jacinta', 'admin@duoc.cl',
 '$2b$10$l0Lx.fhOARhCcccwBhEMKODba2RAm7pqKxTNthCGz0rRU.510BRO2',
 'ADMINISTRADOR', 'Valparaíso', 'Viña del Mar', 'Calle Ejemplo 123'),
('180220333', 'Vendedor', 'Prueba', 'vendedor@duoc.cl',
 '$2b$10$l0Lx.fhOARhCcccwBhEMKODba2RAm7pqKxTNthCGz0rRU.510BRO2',
 'VENDEDOR', 'Valparaíso', 'Viña del Mar', 'Calle Vendedor 456'),
('170330444', 'Cliente', 'Prueba', 'cliente@duoc.cl',
 '$2b$10$l0Lx.fhOARhCcccwBhEMKODba2RAm7pqKxTNthCGz0rRU.510BRO2',
 'CLIENTE', 'Valparaíso', 'Viña del Mar', 'Calle Cliente 789');

INSERT INTO categoria (nombre, descripcion)
VALUES ('Camisas', 'Camisas de manga corta y manga larga para hombre y mujer.');

INSERT INTO producto (nombre, genero, tipo, categoria_id, precio, stock, stock_critico, descripcion, tallas, colores) VALUES
('Camisa manga corta hombre', 'hombre', 'cam_corta', 1, 14990, 25, 5,
 'Camisa de manga corta, corte cómodo para el día a día.',
 '["S","M","L","XL"]', '["azul","rojo"]'),
('Camisa manga larga hombre', 'hombre', 'cam_larga', 1, 17990, 3, 5,
 'Camisa de manga larga, ideal para looks casuales o semiformales.',
 '["S","M","L","XL"]', '["gris","negro","rojo"]'),
('Camisa manga corta mujer', 'mujer', 'cam_corta', 1, 14990, 12, 5,
 'Camisa de manga corta, liviana y fresca.',
 '["S","M","L","XL"]', '["celeste","purpura","rojo"]'),
('Camisa manga larga mujer', 'mujer', 'cam_larga', 1, 17990, 8, 5,
 'Camisa de manga larga en un negro versátil.',
 '["S","M","L","XL"]', '["negro"]');
