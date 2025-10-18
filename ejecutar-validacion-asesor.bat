@echo off
echo Ejecutando script para agregar campo validacion_asesor...
echo.

REM Intentar con diferentes rutas comunes de MySQL
set MYSQL_PATHS="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" "C:\xampp\mysql\bin\mysql.exe" "C:\wamp64\bin\mysql\mysql8.0.21\bin\mysql.exe"

for %%i in (%MYSQL_PATHS%) do (
    if exist %%i (
        echo Encontrado MySQL en: %%i
        echo Ejecutando script...
        %%i -u root -p -e "USE crediactiva; SOURCE agregar-validacion-asesor-cronograma.sql;"
        echo Script ejecutado correctamente.
        pause
        exit /b 0
    )
)

echo No se encontro MySQL en las rutas comunes.
echo Por favor ejecuta manualmente el archivo: agregar-validacion-asesor-cronograma.sql
echo en tu cliente MySQL favorito.
pause
