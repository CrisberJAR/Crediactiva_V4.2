@echo off
echo ====================================
echo   Compilar JAR Ejecutable
echo ====================================
echo.
echo Compilando proyecto...
echo.

mvn clean package

echo.
echo ====================================
echo   Compilacion completada
echo ====================================
echo.
echo El JAR se encuentra en: target\crediactiva-desktop-1.0.0.jar
echo.

pause


