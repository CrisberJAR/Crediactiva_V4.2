-- Script de inicialización de la base de datos CrediActiva
-- Ejecutar después de crear la base de datos

USE crediactiva;

-- Insertar roles por defecto
INSERT INTO roles (id_rol, nombre) VALUES 
(1, 'ADMIN'),
(2, 'ASESOR'),
(3, 'CLIENTE');

-- Insertar usuarios por defecto
INSERT INTO usuarios (id_usuario, password_hash, id_rol, creado_en, activo) VALUES 
(99999999, 'admin123', 1, NOW(), TRUE),
(12345678, 'asesor123', 2, NOW(), TRUE),
(11111111, 'cliente123', 3, NOW(), TRUE);

-- Insertar asesor por defecto
INSERT INTO asesores (id_asesor, nombre, apellido, fecha_contrato, direccion, telefono, email, activo) VALUES 
(12345678, 'Juan', 'Pérez', '2024-01-01', 'Av. Principal 123', '987654321', 'juan.perez@crediactiva.com', TRUE);

-- Insertar cliente por defecto
INSERT INTO clientes (id_cliente, nombre, apellido, fecha_registro, direccion, telefono, email, id_asesor, saldo_capital, etiqueta_cliente, activo) VALUES 
(11111111, 'María', 'González', '2024-01-01', 'Jr. Los Olivos 456', '912345678', 'maria.gonzalez@email.com', 12345678, 500.00, 'excelente', TRUE);

-- Insertar solicitud de préstamo de ejemplo
INSERT INTO prestamos (id_prestamo, id_cliente, id_asesor, monto_solicitado, monto_desembolsado, tasa_interes, estado, etiqueta, periodo_meses, tipo_pago, fecha_inicio, fecha_fin, observacion, creado_en) VALUES 
(1, 11111111, 12345678, 1000.00, 900.00, 18.00, 'pendiente', 'puntual', 1, 'diario', NULL, NULL, 'Préstamo para compra de celular', NOW());

-- Insertar cronograma de ejemplo (26 cuotas diarias sin domingos)
INSERT INTO cronograma (id_prestamo, numero_cuota, fecha_programada, monto_cuota, estado_cuota, fecha_pago_real) VALUES 
-- Enero 2024 (empezando un lunes)
(1, 1, '2024-01-02', 38.46, 'pendiente', NULL),
(1, 2, '2024-01-03', 38.46, 'pendiente', NULL),
(1, 3, '2024-01-04', 38.46, 'pendiente', NULL),
(1, 4, '2024-01-05', 38.46, 'pendiente', NULL),
(1, 5, '2024-01-06', 38.46, 'pendiente', NULL),
(1, 6, '2024-01-08', 38.46, 'pendiente', NULL), -- Lunes (saltando domingo)
(1, 7, '2024-01-09', 38.46, 'pendiente', NULL),
(1, 8, '2024-01-10', 38.46, 'pendiente', NULL),
(1, 9, '2024-01-11', 38.46, 'pendiente', NULL),
(1, 10, '2024-01-12', 38.46, 'pendiente', NULL),
(1, 11, '2024-01-13', 38.46, 'pendiente', NULL),
(1, 12, '2024-01-15', 38.46, 'pendiente', NULL), -- Lunes
(1, 13, '2024-01-16', 38.46, 'pendiente', NULL),
(1, 14, '2024-01-17', 38.46, 'pendiente', NULL),
(1, 15, '2024-01-18', 38.46, 'pendiente', NULL),
(1, 16, '2024-01-19', 38.46, 'pendiente', NULL),
(1, 17, '2024-01-20', 38.46, 'pendiente', NULL),
(1, 18, '2024-01-22', 38.46, 'pendiente', NULL), -- Lunes
(1, 19, '2024-01-23', 38.46, 'pendiente', NULL),
(1, 20, '2024-01-24', 38.46, 'pendiente', NULL),
(1, 21, '2024-01-25', 38.46, 'pendiente', NULL),
(1, 22, '2024-01-26', 38.46, 'pendiente', NULL),
(1, 23, '2024-01-27', 38.46, 'pendiente', NULL),
(1, 24, '2024-01-29', 38.46, 'pendiente', NULL), -- Lunes
(1, 25, '2024-01-30', 38.46, 'pendiente', NULL),
(1, 26, '2024-01-31', 38.46, 'pendiente', NULL);

-- Insertar movimiento de capital (abono del 10%)
INSERT INTO movimientos_capital (id_cliente, tipo_movimiento, monto, fecha, id_admin) VALUES 
(11223344, 'abono', 100.00, NOW(), 12345678);

-- Insertar recaudación de ejemplo
INSERT INTO recaudacion_asesor (id_asesor, id_cliente, id_prestamo, fecha_registro, monto_registrado, validado) VALUES 
(87654321, 11223344, 1, NOW(), 115.38, FALSE);

-- Verificar datos insertados
SELECT 'Roles creados:' as info, COUNT(*) as cantidad FROM roles;
SELECT 'Usuarios creados:' as info, COUNT(*) as cantidad FROM usuarios;
SELECT 'Asesores creados:' as info, COUNT(*) as cantidad FROM asesores;
SELECT 'Clientes creados:' as info, COUNT(*) as cantidad FROM clientes;
SELECT 'Préstamos creados:' as info, COUNT(*) as cantidad FROM prestamos;
SELECT 'Cuotas creadas:' as info, COUNT(*) as cantidad FROM cronograma;
SELECT 'Movimientos de capital:' as info, COUNT(*) as cantidad FROM movimientos_capital;
SELECT 'Recaudaciones:' as info, COUNT(*) as cantidad FROM recaudacion_asesor;

-- Consultas de verificación
SELECT 
    'Cliente de ejemplo:' as tipo,
    CONCAT(c.nombre, ' ', c.apellido) as nombre,
    c.saldo_capital as capital,
    c.etiqueta_cliente as etiqueta
FROM clientes c 
WHERE c.id_cliente = 11223344;

SELECT 
    'Préstamo de ejemplo:' as tipo,
    p.id_prestamo,
    p.monto_solicitado,
    p.estado,
    COUNT(cr.id_cuota) as total_cuotas
FROM prestamos p 
LEFT JOIN cronograma cr ON p.id_prestamo = cr.id_prestamo
WHERE p.id_prestamo = 1
GROUP BY p.id_prestamo;

-- Mensaje de finalización
SELECT 'Base de datos inicializada correctamente' as mensaje;
