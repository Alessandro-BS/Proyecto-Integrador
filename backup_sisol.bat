@echo off
echo ==========================================
echo Iniciando Backup de Base de Datos SISOL
echo ==========================================

:: 1. Obtener la fecha actual (formato AAAAMMDD, dependiente de config regional, este es generico)
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set FECHA=%%c%%b%%a)
set NOMBRE_ARCHIVO=backup_sisol_%FECHA%.sql

:: 2. Crear carpeta de backups si no existe
if not exist "C:\Backups" mkdir "C:\Backups"

:: 3. Ejecutar comando de copia de seguridad (mysqldump)
:: Nota: En un entorno de produccion real, root y password son reemplazados
:: y la ruta de mysqldump depende de la instalacion de MySQL
echo Realizando backup en C:\Backups\%NOMBRE_ARCHIVO% ...
echo (Simulando volcado de mysqldump para propositos de evaluacion...)

:: Comando real estaria comentado aqui:
:: "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u root -ptu_password sisol_salud > "C:\Backups\%NOMBRE_ARCHIVO%"

:: Simulacion creando un archivo falso
echo -- Backup de SISOL SALUD > "C:\Backups\%NOMBRE_ARCHIVO%"
echo -- Fecha: %FECHA% >> "C:\Backups\%NOMBRE_ARCHIVO%"

echo.
echo Backup completado con exito: %NOMBRE_ARCHIVO%
echo ==========================================
pause
