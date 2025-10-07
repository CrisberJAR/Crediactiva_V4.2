# CrediActiva Desktop

Sistema de gestión de préstamos para CrediActiva desarrollado en Java 21 con JavaFX.

## Características

- **Gestión de Clientes**: Registro y administración de clientes con etiquetas de riesgo
- **Gestión de Préstamos**: Solicitudes, aprobación y seguimiento de préstamos
- **Cronogramas**: Generación automática de cronogramas sin domingos
- **Recaudación**: Sistema de borradores y validación de pagos por asesores
- **Reportes PDF**: Cartillas de cronograma, constancias y reportes de recaudación
- **Auditoría**: Trazabilidad completa de todas las operaciones
- **Roles de Usuario**: Administrador, Asesor y Cliente con permisos diferenciados

## Requisitos del Sistema

- **Java 21** o superior
- **MySQL 8.0** o superior
- **Maven 3.8** o superior
- **Windows 10/11** (para el instalador)

## Instalación

### 1. Configuración de la Base de Datos

1. Crear la base de datos MySQL:
```sql
CREATE DATABASE crediactiva;
```

2. Configurar la conexión en `src/main/resources/database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/crediactiva?useSSL=false&serverTimezone=UTC
db.username=root
db.password=root
```

3. Ejecutar el script SQL para crear las tablas (ver archivo `database.sql`)

### 2. Compilación y Ejecución

#### Opción A: Ejecutar desde Maven
```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn javafx:run
```

#### Opción B: Ejecutar JAR
```bash
# Compilar y empaquetar
mvn clean package

# Ejecutar el JAR
java -jar target/crediactiva-desktop-1.0.0.jar
```

### 3. Crear Instalador Windows

```bash
# Crear imagen de runtime
mvn clean package
jlink --module-path "target/modules" --add-modules pe.crediactiva.app --output target/runtime-image

# Crear instalador
mvn jpackage:jpackage
```

El instalador se generará en `target/distributions/`

## Configuración Inicial

### 1. Usuarios por Defecto

El sistema incluye usuarios por defecto:

- **Administrador**: DNI `12345678`, contraseña `admin123`
- **Asesor**: DNI `87654321`, contraseña `asesor123`
- **Cliente**: DNI `11223344`, contraseña `cliente123`

### 2. Configuración de Logs

Los logs se configuran en `src/main/resources/logback.xml` y se guardan en:
- Consola: Para desarrollo
- Archivo: `logs/crediactiva.log`

## Uso del Sistema

### Administrador

1. **Bandeja de Solicitudes**: Revisar, aprobar o rechazar solicitudes de préstamo
2. **Administrar Pagos**: Validar borradores de recaudación y aplicar pagos
3. **Gestión de Usuarios**: Crear y administrar asesores y clientes
4. **Reportes**: Generar reportes de recaudación y constancias

### Asesor

1. **Dashboard**: Ver cuotas del día, vencidas y recaudación mensual
2. **Clientes**: Registrar y gestionar clientes asignados
3. **Solicitar Préstamo**: Crear solicitudes de préstamo para clientes
4. **Recaudación**: Registrar borradores de cobro en campo

### Cliente

1. **Resumen**: Ver capital acumulado y etiqueta de cliente
2. **Préstamos**: Consultar cronogramas y estado de pagos
3. **Simulador**: Calcular cronogramas de préstamos

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/pe/crediactiva/app/
│   │   ├── config/          # Configuración de BD y sesión
│   │   ├── dao/             # Interfaces y DAOs
│   │   ├── model/           # Modelos de datos
│   │   ├── service/         # Lógica de negocio
│   │   ├── view/            # Controladores JavaFX
│   │   ├── util/            # Utilidades
│   │   └── Main.java        # Punto de entrada
│   └── resources/
│       ├── fxml/            # Interfaces JavaFX
│       ├── css/             # Estilos
│       ├── database.properties
│       └── logback.xml
└── test/                    # Pruebas unitarias
```

## Reglas de Negocio

### Préstamos
- Capital retenido: 10% del monto solicitado
- Cronogramas sin domingos
- Cuotas diarias por defecto (26 cuotas para 1 mes)

### Recaudación
- Asesores registran borradores en campo
- Administradores validan y aplican pagos
- Sueldo asesor: 10% de recaudación mensual

### Capital del Cliente
- Abono: 10% de cada préstamo
- Desembolso: máximo 50% del capital acumulado

## Desarrollo

### Ejecutar Pruebas
```bash
mvn test
```

### Generar Documentación
```bash
mvn javadoc:javadoc
```

### Limpiar Proyecto
```bash
mvn clean
```

## Solución de Problemas

### Error de Conexión a BD
- Verificar que MySQL esté ejecutándose
- Revisar credenciales en `database.properties`
- Verificar que la base de datos `crediactiva` existe

### Error de JavaFX
- Asegurar que Java 21 esté instalado
- Verificar que el módulo JavaFX esté disponible

### Error de Permisos
- Ejecutar como administrador si es necesario
- Verificar permisos de escritura en directorios `data/`

## Soporte

Para soporte técnico o reportar bugs, contactar al equipo de desarrollo.

## Licencia

Este software es propiedad de CrediActiva y está destinado para uso interno.

---

**CrediActiva Desktop v1.0.0** - Sistema de Gestión de Préstamos
