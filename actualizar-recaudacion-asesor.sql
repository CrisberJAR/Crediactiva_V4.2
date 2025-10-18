-- Script para actualizar la tabla recaudacion_asesor
-- Agregar campo id_cuota para identificar cuotas específicas

USE crediactiva;

-- Agregar campo id_cuota a la tabla recaudacion_asesor
ALTER TABLE recaudacion_asesor 
ADD COLUMN id_cuota BIGINT NULL AFTER id_prestamo;

-- Agregar campo observaciones para información adicional
ALTER TABLE recaudacion_asesor 
ADD COLUMN observaciones TEXT NULL AFTER validado;

-- Crear índice para mejorar consultas por cuota
CREATE INDEX idx_recaudacion_asesor_cuota ON recaudacion_asesor(id_cuota);

-- Agregar foreign key constraint (opcional, descomentar si es necesario)
-- ALTER TABLE recaudacion_asesor 
-- ADD CONSTRAINT fk_recaudacion_cuota 
-- FOREIGN KEY (id_cuota) REFERENCES cronograma(id_cuota);

-- Actualizar registros existentes (si es necesario)
-- UPDATE recaudacion_asesor SET id_cuota = NULL WHERE id_cuota IS NULL;

-- Verificar la nueva estructura
DESCRIBE recaudacion_asesor;

-- Mostrar mensaje de confirmación
SELECT 'Tabla recaudacion_asesor actualizada correctamente' as mensaje;
