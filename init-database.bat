@echo off
echo Inicializando base de datos CrediActiva...

REM Verificar si MySQL está ejecutándose
echo Verificando conexion a MySQL...

REM Crear base de datos si no existe
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS crediactiva;" 2>nul

if %errorlevel% neq 0 (
    echo Error: No se puede conectar a MySQL con usuario root y contraseña root
    echo Por favor verifica que MySQL esté ejecutándose y las credenciales sean correctas
    pause
    exit /b 1
)

REM Ejecutar script SQL
echo Ejecutando script de inicializacion...
mysql -u root -proot crediactiva < database.sql

if %errorlevel% equ 0 (
    echo Base de datos inicializada correctamente!
    echo.
    echo Usuarios de prueba creados:
    echo - Admin: usuario=1, password=admin123
    echo - Asesor: usuario=2, password=asesor123  
    echo - Cliente: usuario=3, password=cliente123
) else (
    echo Error al ejecutar el script SQL
)

pause
