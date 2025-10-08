@echo off
echo ========================================
echo Insertando Prestamos de Prueba
echo ========================================
echo.

REM Leer las propiedades de la base de datos
for /f "tokens=1,2 delims==" %%a in (src\main\resources\database.properties) do (
    if "%%a"=="db.host" set DB_HOST=%%b
    if "%%a"=="db.port" set DB_PORT=%%b
    if "%%a"=="db.name" set DB_NAME=%%b
    if "%%a"=="db.user" set DB_USER=%%b
    if "%%a"=="db.password" set DB_PASSWORD=%%b
)

echo Host: %DB_HOST%
echo Puerto: %DB_PORT%
echo Base de datos: %DB_NAME%
echo Usuario: %DB_USER%
echo.

REM Ejecutar el script SQL
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < insertar-prestamos-prueba.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Datos insertados correctamente
    echo ========================================
    echo.
    echo Ahora puedes ejecutar la aplicacion y revisar las solicitudes pendientes.
) else (
    echo.
    echo ========================================
    echo Error al insertar los datos
    echo ========================================
    echo Verifica que MySQL este ejecutandose y las credenciales sean correctas.
)

echo.
pause

