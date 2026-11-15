-- Agregar campos sin borrar datos ni repetir columnas.
SET @sql_telefono = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'telefono'),
    'SELECT 1',
    'ALTER TABLE usuario ADD COLUMN telefono VARCHAR(25) NULL'
);
PREPARE agregar_telefono FROM @sql_telefono;
EXECUTE agregar_telefono;
DEALLOCATE PREPARE agregar_telefono;

SET @sql_activo = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'activo'),
    'SELECT 1',
    'ALTER TABLE usuario ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE'
);
PREPARE agregar_activo FROM @sql_activo;
EXECUTE agregar_activo;
DEALLOCATE PREPARE agregar_activo;
