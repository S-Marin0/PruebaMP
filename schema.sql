-- Database Schema for EventMaster Project (MySQL)

-- Usuario Table
-- Stores both Asistente and Organizador, differentiated by tipo_usuario
CREATE TABLE IF NOT EXISTS `usuario` (
    `id` VARCHAR(255) PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL, -- NB: Store hashed passwords in a real application!
    `tipo_usuario` VARCHAR(50) NOT NULL -- e.g., 'ASISTENTE', 'ORGANIZADOR'
    -- Preferences for Usuario would likely be in a separate join table (e.g., usuario_preferencias)
    -- HistorialCompras is derived from the Compra table
);

-- Lugar Table
CREATE TABLE IF NOT EXISTS `lugar` (
    `id` VARCHAR(255) PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL,
    `direccion` TEXT,
    -- For complex fields like tiposEventosAdmitidos, reservasPorFranja, subcomponentes:
    -- These are not fully handled by the basic LugarDAOImplMySQL.
    -- Option 1: JSON/TEXT columns (shown here as placeholders, querying is difficult)
    `tipos_eventos_admitidos` TEXT, -- Placeholder for JSON array of strings
    `reservas_por_franja` TEXT,     -- Placeholder for JSON map
    -- Option 2: Separate normalized tables (preferred for relational integrity and querying)
    -- e.g., lugar_tipos_eventos (lugar_id, tipo_evento_str)
    -- e.g., lugar_subcomponentes (parent_lugar_id, child_component_id, component_type)
    -- Capacidad total is often derived from subcomponentes or defined directly.
    -- If a Lugar itself has a base capacity not from subcomponents, add a capacity column here.
    `capacidad_base` INT DEFAULT 0
);
-- Example for subcomponentes if Lugar can contain other Lugares (simplified hierarchical)
-- ALTER TABLE `lugar` ADD COLUMN `parent_lugar_id` VARCHAR(255) NULL;
-- ALTER TABLE `lugar` ADD CONSTRAINT `fk_lugar_parent` FOREIGN KEY (`parent_lugar_id`) REFERENCES `lugar` (`id`) ON DELETE SET NULL;


-- Evento Table
CREATE TABLE IF NOT EXISTS `evento` (
    `id` VARCHAR(255) PRIMARY KEY,
    `nombre` VARCHAR(255) NOT NULL,
    `descripcion` TEXT,
    `categoria` VARCHAR(100),
    `fecha_hora` DATETIME NOT NULL,
    `lugar_id` VARCHAR(255) NOT NULL,
    `organizador_id` VARCHAR(255) NOT NULL,
    `capacidad_total` INT DEFAULT 0,
    `entradas_vendidas` INT DEFAULT 0,
    `estado_actual` VARCHAR(50) NOT NULL, -- e.g., "BORRADOR", "PUBLICADO", "CANCELADO", "EN_CURSO", "FINALIZADO"
    -- urlsImagenes, urlsVideos, tiposEntradaDisponibles would typically be in related tables
    -- e.g., evento_imagenes (evento_id, image_url)
    -- e.g., evento_videos (evento_id, video_url)
    -- Tipos de entrada definitions are in tipo_entrada_definicion table
    FOREIGN KEY (`lugar_id`) REFERENCES `lugar` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`organizador_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT -- Organizador must be a Usuario
);

-- TipoEntradaDefinicion Table (Defines the types of tickets available for an event)
CREATE TABLE IF NOT EXISTS `tipo_entrada_definicion` (
    `id` VARCHAR(255) PRIMARY KEY, -- Unique ID for this specific definition instance
    `evento_id` VARCHAR(255) NOT NULL,
    `nombre_tipo` VARCHAR(100) NOT NULL, -- e.g., "General", "VIP"
    `precio_base` DECIMAL(10, 2) NOT NULL,
    `cantidad_total` INT NOT NULL,
    `cantidad_disponible` INT NOT NULL,
    `limite_compra_por_usuario` INT DEFAULT 1000, -- Or some other high number for effectively no limit
    `beneficios_extra` TEXT, -- Placeholder for JSON array of strings

    -- Decorator related fields from TipoEntrada model
    `ofrece_mercancia` BOOLEAN DEFAULT FALSE,
    `desc_mercancia` VARCHAR(255),
    `precio_mercancia` DECIMAL(10, 2) DEFAULT 0.00,
    `ofrece_descuento` BOOLEAN DEFAULT FALSE,
    `desc_descuento` VARCHAR(255),
    `monto_descuento` DECIMAL(10, 2) DEFAULT 0.00,

    FOREIGN KEY (`evento_id`) REFERENCES `evento` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uq_evento_nombretipo` (`evento_id`, `nombre_tipo`) -- A ticket type name should be unique within an event
);

-- Compra Table
CREATE TABLE IF NOT EXISTS `compra` (
    `id` VARCHAR(255) PRIMARY KEY,
    `usuario_id` VARCHAR(255) NOT NULL,
    `evento_id` VARCHAR(255) NOT NULL, -- The primary event of the purchase (denormalized for easy query, or could be derived from entradas)
    `total_pagado` DECIMAL(10, 2) NOT NULL,
    `fecha_compra` DATETIME NOT NULL,
    `estado_compra` VARCHAR(50) NOT NULL, -- e.g., "PENDIENTE_PAGO", "COMPLETADA", "CANCELADA"
    `id_transaccion_pasarela` VARCHAR(255),
    FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`evento_id`) REFERENCES `evento` (`id`) ON DELETE RESTRICT
    -- HistorialOperaciones (Commands) would be complex to store relationally; maybe a separate log table or serialized.
);

-- EntradaVendida Table (Individual sold tickets)
CREATE TABLE IF NOT EXISTS `entrada_vendida` (
    `id` VARCHAR(255) PRIMARY KEY, -- Unique ID for this specific sold ticket instance
    `evento_id` VARCHAR(255) NOT NULL,
    `compra_id` VARCHAR(255), -- Nullable if an entry can exist outside a direct purchase (e.g. complimentary)
                                -- Or NOT NULL if every ticket *must* belong to a purchase.
    -- `tipo_entrada_definicion_id` VARCHAR(255) NOT NULL, -- FK to tipo_entrada_definicion.id
    `tipo_entrada_nombre` VARCHAR(100) NOT NULL, -- Denormalized name of the type (e.g. "VIP") for convenience
    `precio_final` DECIMAL(10, 2) NOT NULL, -- Actual price paid for this ticket
    `descripcion_final` TEXT, -- Final description (e.g., including decorator info)
    -- Could add other fields like qr_code_data, check_in_status, etc.
    FOREIGN KEY (`evento_id`) REFERENCES `evento` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`compra_id`) REFERENCES `compra` (`id`) ON DELETE SET NULL -- Or CASCADE if entries are deleted with compra
    -- FOREIGN KEY (`tipo_entrada_definicion_id`) REFERENCES `tipo_entrada_definicion` (`id`) ON DELETE RESTRICT
);


-- Promocion Table
CREATE TABLE IF NOT EXISTS `promocion` (
    `id` VARCHAR(255) PRIMARY KEY,
    `descripcion` VARCHAR(255) NOT NULL,
    `porcentaje_descuento` DECIMAL(5, 4) NOT NULL, -- e.g., 0.1000 for 10.00%
    `fecha_inicio` DATETIME NOT NULL,
    `fecha_fin` DATETIME NOT NULL,
    `tipo_aplicable` VARCHAR(50), -- "Evento", "TipoEntrada", "UsuarioEspecifico"
    `id_aplicable` VARCHAR(255)   -- EventoID, TipoEntradaDefinicionID (or nombre_tipo if unique enough), UsuarioID
);

-- CodigoDescuento Table
CREATE TABLE IF NOT EXISTS `codigo_descuento` (
    `codigo_str` VARCHAR(100) PRIMARY KEY, -- The literal code string users enter
    `porcentaje_descuento` DECIMAL(5, 4) NOT NULL,
    `fecha_expiracion` DATETIME,
    `usos_maximos` INT DEFAULT 1,
    `usos_actuales` INT DEFAULT 0,
    `activo` BOOLEAN DEFAULT TRUE,
    `promocion_asociada_id` VARCHAR(255), -- Optional FK to Promocion table
    FOREIGN KEY (`promocion_asociada_id`) REFERENCES `promocion` (`id`) ON DELETE SET NULL
);

-- --- Potentially missing tables for complex relationships / features: ---

-- `usuario_preferencias` (usuario_id, preferencia_tag_o_categoria_id)
-- `evento_imagenes` (id, evento_id, image_url, es_principal)
-- `evento_videos` (id, evento_id, video_url)
-- `lugar_secciones` (id, lugar_id, nombre_seccion, capacidad_seccion) if using Composite for Lugar in DB
-- `compra_historial_operaciones` (id, compra_id, comando_nombre, comando_data_json, timestamp)

-- --- Indexes for Performance (examples) ---
CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_evento_fecha ON evento(fecha_hora);
CREATE INDEX idx_evento_lugar ON evento(lugar_id);
CREATE INDEX idx_evento_organizador ON evento(organizador_id);
CREATE INDEX idx_tipo_entrada_evento ON tipo_entrada_definicion(evento_id);
CREATE INDEX idx_compra_usuario ON compra(usuario_id);
CREATE INDEX idx_compra_evento ON compra(evento_id);
CREATE INDEX idx_entrada_vendida_evento ON entrada_vendida(evento_id);
CREATE INDEX idx_entrada_vendida_compra ON entrada_vendida(compra_id);
CREATE INDEX idx_promocion_fechas ON promocion(fecha_inicio, fecha_fin);
CREATE INDEX idx_codigo_descuento_activo_fecha ON codigo_descuento(activo, fecha_expiracion);

