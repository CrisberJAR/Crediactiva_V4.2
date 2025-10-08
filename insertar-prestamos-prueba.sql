-- Script para insertar préstamos de prueba en estado pendiente
-- Este script verifica que existan usuarios, asesores y clientes antes de insertar préstamos

-- Verificar que existan asesores
INSERT INTO asesores (id_asesor, nombre, apellido, fecha_contrato, activo)
SELECT 12345678, 'Carlos', 'Ramirez', '2024-01-15', 1
WHERE NOT EXISTS (SELECT 1 FROM asesores WHERE id_asesor = 12345678);

INSERT INTO asesores (id_asesor, nombre, apellido, fecha_contrato, activo)
SELECT 87654321, 'Maria', 'Torres', '2024-02-01', 1
WHERE NOT EXISTS (SELECT 1 FROM asesores WHERE id_asesor = 87654321);

-- Verificar que existan clientes
INSERT INTO clientes (id_cliente, nombre, apellido, dni, fecha_registro, id_asesor, saldo_capital, etiqueta_cliente, activo)
SELECT 20304050, 'Juan', 'Pérez', '20304050', '2024-03-01', 12345678, 0, 'excelente', 1
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = 20304050);

INSERT INTO clientes (id_cliente, nombre, apellido, dni, fecha_registro, id_asesor, saldo_capital, etiqueta_cliente, activo)
SELECT 30405060, 'Ana', 'García', '30405060', '2024-03-05', 87654321, 0, 'excelente', 1
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = 30405060);

INSERT INTO clientes (id_cliente, nombre, apellido, dni, fecha_registro, id_asesor, saldo_capital, etiqueta_cliente, activo)
SELECT 40506070, 'Luis', 'Martínez', '40506070', '2024-03-10', 12345678, 0, 'deficiente', 1
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE id_cliente = 40506070);

-- Insertar préstamos pendientes de prueba
INSERT INTO prestamos (id_cliente, id_asesor, monto_solicitado, tasa_interes, periodo_meses, tipo_pago, estado, observacion)
SELECT 20304050, 12345678, 5000.00, 15.00, 3, 'diario', 'pendiente', 'Primera solicitud del cliente'
WHERE NOT EXISTS (
    SELECT 1 FROM prestamos 
    WHERE id_cliente = 20304050 AND estado = 'pendiente'
);

INSERT INTO prestamos (id_cliente, id_asesor, monto_solicitado, tasa_interes, periodo_meses, tipo_pago, estado, observacion)
SELECT 30405060, 87654321, 8000.00, 18.00, 4, 'diario', 'pendiente', 'Cliente con buen historial crediticio'
WHERE NOT EXISTS (
    SELECT 1 FROM prestamos 
    WHERE id_cliente = 30405060 AND estado = 'pendiente'
);

INSERT INTO prestamos (id_cliente, id_asesor, monto_solicitado, tasa_interes, periodo_meses, tipo_pago, estado, observacion)
SELECT 40506070, 12345678, 3000.00, 20.00, 2, 'diario', 'pendiente', 'Requiere análisis adicional'
WHERE NOT EXISTS (
    SELECT 1 FROM prestamos 
    WHERE id_cliente = 40506070 AND estado = 'pendiente'
);

-- Verificar los registros insertados
SELECT 'Préstamos pendientes insertados:' as info;
SELECT p.id_prestamo, 
       CONCAT(c.nombre, ' ', c.apellido) as cliente,
       CONCAT(a.nombre, ' ', a.apellido) as asesor,
       p.monto_solicitado,
       p.tasa_interes,
       p.periodo_meses,
       p.estado,
       p.observacion
FROM prestamos p
JOIN clientes c ON p.id_cliente = c.id_cliente
JOIN asesores a ON p.id_asesor = a.id_asesor
WHERE p.estado = 'pendiente'
ORDER BY p.creado_en DESC;

