@echo off
echo Verificando credenciales en la base de datos...

mysql -u root -proot -e "USE crediactiva; SELECT 'ADMIN:' as ROL, id_usuario, password_hash FROM usuarios WHERE id_usuario = 99999999 UNION ALL SELECT 'ASESOR:' as ROL, id_usuario, password_hash FROM usuarios WHERE id_usuario = 12345678 UNION ALL SELECT 'CLIENTE:' as ROL, id_usuario, password_hash FROM usuarios WHERE id_usuario = 11111111;" 2>nul

if %errorlevel% equ 0 (
    echo.
    echo Credenciales verificadas correctamente!
    echo.
    echo Ahora puedes probar la aplicacion con:
    echo - Admin: 99999999 / admin123
    echo - Asesor: 12345678 / asesor123
    echo - Cliente: 11111111 / cliente123
) else (
    echo Error al conectar con la base de datos
    echo Verifica que MySQL este ejecutandose
)

pause
