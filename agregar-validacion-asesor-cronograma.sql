-- Agregar campo validacion_asesor a la tabla cronograma
-- Este campo controla si una cuota ya tiene un registro de recaudación pendiente

ALTER TABLE cronograma 
ADD COLUMN validacion_asesor TINYINT(1) DEFAULT 0 COMMENT '1=cuota con recaudación pendiente, 0=sin recaudación';

-- Actualizar registros existentes para que todos empiecen en 0
UPDATE cronograma SET validacion_asesor = 0 WHERE validacion_asesor IS NULL;

-- Crear índice para mejorar performance de consultas
CREATE INDEX idx_cronograma_validacion_asesor ON cronograma(validacion_asesor);

-- Ejemplo de consulta para verificar cuotas disponibles para recaudación:
-- SELECT * FROM cronograma WHERE id_prestamo = ? AND validacion_asesor = 0 AND estado_cuota IN ('PENDIENTE', 'RETRASADA');
