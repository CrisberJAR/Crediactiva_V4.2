@echo off
echo Reinicializando base de datos CrediActiva con credenciales correctas...

REM Eliminar y recrear la base de datos
echo Eliminando base de datos existente...
mysql -u root -proot -e "DROP DATABASE IF EXISTS crediactiva;" 2>nul

echo Creando nueva base de datos...
mysql -u root -proot -e "CREATE DATABASE crediactiva;" 2>nul

if %errorlevel% neq 0 (
    echo Error: No se puede conectar a MySQL con usuario root y contraseña root
    echo Por favor verifica que MySQL esté ejecutándose y las credenciales sean correctas
    pause
    exit /b 1
)

REM Ejecutar script SQL actualizado
echo Ejecutando script de inicializacion actualizado...
mysql -u root -proot crediactiva < database.sql

if %errorlevel% equ 0 (
    echo Base de datos reinicializada correctamente!
    echo.
    echo Credenciales actualizadas:
    echo - Admin: usuario=99999999, password=admin123
    echo - Asesor: usuario=12345678, password=asesor123  
    echo - Cliente: usuario=11111111, password=cliente123
    echo.
    echo Ahora puedes probar la aplicacion con estas credenciales.
) else (
    echo Error al ejecutar el script SQL
)

pause
