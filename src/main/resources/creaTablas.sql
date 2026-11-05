-- Nexo: ejecutar en MySQL 8 con una cuenta autorizada. No elimina datos.
CREATE DATABASE IF NOT EXISTS nexo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nexo;
CREATE TABLE IF NOT EXISTS fuente_pago (
 id_fuente_pago INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL, codigo VARCHAR(30) NOT NULL UNIQUE,
 moneda VARCHAR(3) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS rol (
 id_rol BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 codigo VARCHAR(20) NOT NULL UNIQUE, descripcion VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS usuario (
 id_usuario BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL, correo VARCHAR(150) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL,
 fecha_creacion DATETIME(6) NOT NULL
);
CREATE TABLE IF NOT EXISTS usuario_rol (
 id_usuario BIGINT NOT NULL, id_rol BIGINT NOT NULL,
 PRIMARY KEY(id_usuario,id_rol),
 FOREIGN KEY(id_usuario) REFERENCES usuario(id_usuario),
 FOREIGN KEY(id_rol) REFERENCES rol(id_rol)
);
CREATE TABLE IF NOT EXISTS lote_importacion (
 id_lote_importacion BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_fuente_pago INT NOT NULL, id_usuario BIGINT NOT NULL,
 origen VARCHAR(20) NOT NULL, archivo VARCHAR(200) NOT NULL, huella VARCHAR(64) NOT NULL,
 fecha DATETIME(6) NOT NULL, estado VARCHAR(20) NOT NULL, cantidad INT NOT NULL,
 resultado VARCHAR(2000) NOT NULL,
 FOREIGN KEY(id_fuente_pago) REFERENCES fuente_pago(id_fuente_pago),
 FOREIGN KEY(id_usuario) REFERENCES usuario(id_usuario)
);
CREATE TABLE IF NOT EXISTS operacion_interna (
 id_operacion_interna BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_lote_importacion BIGINT NOT NULL, id_fuente_pago INT NOT NULL,
 referencia_liquidacion VARCHAR(80) NOT NULL, referencia VARCHAR(80) NOT NULL,
 tipo VARCHAR(20) NOT NULL, fecha DATE NOT NULL, importe DECIMAL(15,2) NOT NULL,
 moneda VARCHAR(3) NOT NULL, referencia_original VARCHAR(80),
 UNIQUE(id_fuente_pago,referencia,tipo),
 FOREIGN KEY(id_lote_importacion) REFERENCES lote_importacion(id_lote_importacion),
 FOREIGN KEY(id_fuente_pago) REFERENCES fuente_pago(id_fuente_pago)
);
CREATE TABLE IF NOT EXISTS liquidacion (
 id_liquidacion BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_lote_importacion BIGINT NOT NULL, id_fuente_pago INT NOT NULL,
 referencia VARCHAR(80) NOT NULL, fecha DATE NOT NULL,
 importe_reportado DECIMAL(15,2) NOT NULL, moneda VARCHAR(3) NOT NULL,
 UNIQUE(id_fuente_pago,referencia),
 FOREIGN KEY(id_lote_importacion) REFERENCES lote_importacion(id_lote_importacion),
 FOREIGN KEY(id_fuente_pago) REFERENCES fuente_pago(id_fuente_pago)
);
CREATE TABLE IF NOT EXISTS movimiento_proveedor (
 id_movimiento_proveedor BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_lote_importacion BIGINT NOT NULL, id_fuente_pago INT NOT NULL, id_liquidacion BIGINT NOT NULL,
 referencia VARCHAR(80) NOT NULL, tipo VARCHAR(20) NOT NULL, fecha DATE NOT NULL,
 importe DECIMAL(15,2) NOT NULL, moneda VARCHAR(3) NOT NULL, referencia_original VARCHAR(80),
 UNIQUE(id_fuente_pago,referencia,tipo),
 FOREIGN KEY(id_lote_importacion) REFERENCES lote_importacion(id_lote_importacion),
 FOREIGN KEY(id_fuente_pago) REFERENCES fuente_pago(id_fuente_pago),
 FOREIGN KEY(id_liquidacion) REFERENCES liquidacion(id_liquidacion)
);
CREATE TABLE IF NOT EXISTS conciliacion (
 id_conciliacion BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_liquidacion BIGINT NOT NULL UNIQUE, id_creador BIGINT NOT NULL, id_autorizante BIGINT,
 fecha DATETIME(6) NOT NULL, fecha_cierre DATETIME(6), estado VARCHAR(20) NOT NULL,
 total_calculado DECIMAL(15,2) NOT NULL, total_interno DECIMAL(15,2) NOT NULL,
 diferencia_total DECIMAL(15,2) NOT NULL,
 FOREIGN KEY(id_liquidacion) REFERENCES liquidacion(id_liquidacion),
 FOREIGN KEY(id_creador) REFERENCES usuario(id_usuario),
 FOREIGN KEY(id_autorizante) REFERENCES usuario(id_usuario)
);
CREATE TABLE IF NOT EXISTS vinculo_conciliacion (
 id_vinculo_conciliacion BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_conciliacion BIGINT NOT NULL, id_operacion_interna BIGINT NOT NULL UNIQUE,
 id_movimiento_proveedor BIGINT NOT NULL UNIQUE, id_usuario BIGINT NOT NULL,
 fecha DATETIME(6) NOT NULL, criterio VARCHAR(50) NOT NULL,
 FOREIGN KEY(id_conciliacion) REFERENCES conciliacion(id_conciliacion),
 FOREIGN KEY(id_operacion_interna) REFERENCES operacion_interna(id_operacion_interna),
 FOREIGN KEY(id_movimiento_proveedor) REFERENCES movimiento_proveedor(id_movimiento_proveedor),
 FOREIGN KEY(id_usuario) REFERENCES usuario(id_usuario)
);
CREATE TABLE IF NOT EXISTS diferencia (
 id_diferencia BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_conciliacion BIGINT NOT NULL, id_operacion_interna BIGINT, id_movimiento_proveedor BIGINT,
 tipo VARCHAR(30) NOT NULL, descripcion VARCHAR(500) NOT NULL,
 estado VARCHAR(25) NOT NULL, importe DECIMAL(15,2),
 FOREIGN KEY(id_conciliacion) REFERENCES conciliacion(id_conciliacion),
 FOREIGN KEY(id_operacion_interna) REFERENCES operacion_interna(id_operacion_interna),
 FOREIGN KEY(id_movimiento_proveedor) REFERENCES movimiento_proveedor(id_movimiento_proveedor)
);
CREATE TABLE IF NOT EXISTS resolucion_diferencia (
 id_resolucion_diferencia BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_diferencia BIGINT NOT NULL, id_proponente BIGINT NOT NULL, id_evaluador BIGINT,
 clasificacion VARCHAR(30) NOT NULL, explicacion VARCHAR(2000) NOT NULL,
 fecha DATETIME(6) NOT NULL, fecha_revision DATETIME(6), estado VARCHAR(20) NOT NULL,
 motivo_rechazo VARCHAR(2000),
 FOREIGN KEY(id_diferencia) REFERENCES diferencia(id_diferencia),
 FOREIGN KEY(id_proponente) REFERENCES usuario(id_usuario),
 FOREIGN KEY(id_evaluador) REFERENCES usuario(id_usuario)
);