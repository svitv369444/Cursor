@echo off
echo ==========================================
echo   Сборка APK для Швейного производства
echo ==========================================
echo.

echo Проверка Gradle...
if not exist gradlew.bat (
    echo ОШИБКА: gradlew.bat не найден!
    echo Убедитесь, что вы находитесь в корневой папке проекта.
    pause
    exit /b 1
)

echo.
echo Очистка предыдущих сборок...
call gradlew clean

echo.
echo Сборка Debug APK...
call gradlew assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo ОШИБКА: Не удалось собрать Debug APK!
    pause
    exit /b 1
)

echo.
echo Сборка Release APK...
call gradlew assembleRelease

if %errorlevel% neq 0 (
    echo.
    echo ПРЕДУПРЕЖДЕНИЕ: Не удалось собрать Release APK (возможно, нет подписи)
    echo Но Debug APK собран успешно!
)

echo.
echo ==========================================
echo             СБОРКА ЗАВЕРШЕНА!
echo ==========================================
echo.
echo APK файлы находятся в:
echo Debug:   app\build\outputs\apk\debug\app-debug.apk
echo Release: app\build\outputs\apk\release\app-release-unsigned.apk
echo.
echo Для установки на устройство используйте Debug версию.
echo.
pause