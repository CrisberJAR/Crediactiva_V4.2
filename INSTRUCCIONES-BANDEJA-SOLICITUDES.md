# Instrucciones para Bandeja de Solicitudes Pendientes

## Problema Resuelto

Se ha corregido el problema donde al hacer clic en "Revisar Solicitudes Pendientes" no se mostraba nada. 

## Mejoras Implementadas

### 1. **Visualización Correcta del Panel**
- Se agregaron dimensiones mínimas y preferidas al BorderPane de la vista
- Ahora el panel se visualizará correctamente con un tamaño de 1000x700 píxeles

### 2. **Mensaje Informativo**
- Si no hay solicitudes pendientes, se mostrará un mensaje indicando que no hay solicitudes en ese momento
- El mensaje explica que las solicitudes aparecerán cuando los asesores las registren

### 3. **Sistema de Caché Mejorado**
- Los nombres de clientes y asesores se cargan en caché para mejorar el rendimiento
- La tabla muestra nombres completos en lugar de solo IDs

### 4. **Filtrado Avanzado**
- Filtrado por asesor, estado, etiqueta de cliente y rango de montos
- Los filtros se pueden combinar para búsquedas más específicas

## Cómo Probar la Funcionalidad

### Opción 1: Usando el Script Automático

1. **Ejecutar el script de inserción de datos de prueba:**
   ```
   insertar-datos-prueba.bat
   ```
   
   Este script insertará automáticamente:
   - 2 asesores de prueba (Carlos Ramirez y Maria Torres)
   - 3 clientes de prueba (Juan Pérez, Ana García, Luis Martínez)
   - 3 préstamos pendientes con diferentes montos y características

2. **Ejecutar la aplicación:**
   ```
   mvn javafx:run
   ```

3. **Iniciar sesión como administrador** (usa las credenciales de tu base de datos)

4. **Hacer clic en el botón "Revisar Solicitudes Pendientes"** o en el menú **Solicitudes → Bandeja de Solicitudes**

### Opción 2: Manual (Ejecutando SQL directamente)

Si prefieres ejecutar el SQL manualmente:

1. Abre MySQL Workbench o tu cliente MySQL preferido
2. Conecta a tu base de datos
3. Ejecuta el contenido del archivo `insertar-prestamos-prueba.sql`

### Opción 3: Crear Solicitud desde el Sistema

Si ya tienes asesores y clientes en tu sistema:

1. Inicia sesión como **Asesor**
2. Ve a **Solicitudes → Nueva Solicitud** o **Solicitar Préstamo**
3. Completa el formulario con los datos del cliente
4. Guarda la solicitud
5. Cierra sesión e inicia como **Administrador**
6. Ve a **Solicitudes → Bandeja de Solicitudes**

## Verificación de Datos Existentes

Para verificar si ya tienes préstamos pendientes en la base de datos:

```sql
SELECT p.id_prestamo, 
       CONCAT(c.nombre, ' ', c.apellido) as cliente,
       CONCAT(a.nombre, ' ', a.apellido) as asesor,
       p.monto_solicitado,
       p.estado,
       p.creado_en
FROM prestamos p
JOIN clientes c ON p.id_cliente = c.id_cliente
JOIN asesores a ON p.id_asesor = a.id_asesor
WHERE p.estado = 'pendiente'
ORDER BY p.creado_en DESC;
```

## Funcionalidades Disponibles en la Bandeja

Una vez que tengas solicitudes pendientes, podrás:

### Ver Solicitudes
- Lista completa con nombres de clientes y asesores
- Montos formateados (S/ 5,000.00)
- Tasas de interés con formato (15.50%)
- Estado y etiqueta de cada cliente
- Fecha de creación

### Filtrar Solicitudes
- **Por Asesor:** Selecciona un asesor específico
- **Por Estado:** PENDIENTE, ACTIVO, SUSPENDIDO, etc.
- **Por Etiqueta:** EXCELENTE, DEFICIENTE, PELIGROSO
- **Por Monto:** Define rango mínimo y máximo

### Acciones sobre Solicitudes
1. **Aprobar y Generar Cronograma:**
   - Modifica la tasa de interés si es necesario
   - Ajusta el período de meses
   - Define el tipo de pago (diario/semanal/mensual)
   - El sistema generará automáticamente el cronograma de pagos

2. **Rechazar:**
   - Ingresa el motivo del rechazo
   - La solicitud cambiará a estado "rechazado"

3. **Guardar Cambios:**
   - Modifica parámetros de la solicitud antes de aprobar
   - Los cambios se guardan en la base de datos

4. **Ver Historial Cliente:**
   - (Próximamente) Visualiza el historial completo del cliente

## Solución de Problemas

### No se muestra la bandeja de solicitudes
- **Causa:** Error al cargar la vista FXML
- **Solución:** 
  - Verifica los logs en `logs/crediactiva.log`
  - Asegúrate de que el proyecto esté compilado: `mvn clean compile`
  - Reinicia la aplicación

### Aparece mensaje "No hay solicitudes pendientes"
- **Causa:** No hay préstamos en estado 'pendiente' en la base de datos
- **Solución:** 
  - Ejecuta `insertar-datos-prueba.bat` para insertar datos de prueba
  - O crea una nueva solicitud como asesor

### Los nombres aparecen como "Cliente 12345"
- **Causa:** El cliente o asesor no existe en la base de datos
- **Solución:**
  - Verifica la integridad referencial en tu base de datos
  - Ejecuta el script de datos de prueba que crea asesores y clientes automáticamente

### Error de conexión a la base de datos
- **Causa:** MySQL no está ejecutándose o las credenciales son incorrectas
- **Solución:**
  - Verifica que MySQL esté ejecutándose
  - Revisa las credenciales en `src/main/resources/database.properties`
  - Ejecuta el script `init-database.bat` si es necesario

## Estructura de Estados de Préstamos

Los préstamos tienen los siguientes estados:

1. **PENDIENTE:** Solicitud recién creada, esperando aprobación del administrador
2. **ACTIVO:** Préstamo aprobado y con cronograma generado
3. **SUSPENDIDO:** Préstamo temporalmente suspendido
4. **FINALIZADO:** Préstamo completamente pagado
5. **RECHAZADO:** Solicitud rechazada por el administrador

## Logs y Depuración

Si encuentras problemas, revisa los logs en:
```
logs/crediactiva.log
```

Los logs te mostrarán:
- Cuántas solicitudes se cargaron
- Errores de conexión a la base de datos
- Problemas al cargar la vista FXML
- Operaciones realizadas (aprobar, rechazar, etc.)

## Próximas Mejoras

- [ ] Visualización del historial completo del cliente
- [ ] Exportación de solicitudes a PDF/Excel
- [ ] Notificaciones automáticas de nuevas solicitudes
- [ ] Análisis de riesgo automatizado
- [ ] Comentarios y seguimiento de solicitudes

---

**Nota:** Todos los cambios realizados están registrados en el control de versiones y se han compilado exitosamente.

