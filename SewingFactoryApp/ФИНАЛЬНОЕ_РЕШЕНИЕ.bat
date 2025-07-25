@echo off
echo ==========================================
echo    ФИНАЛЬНОЕ РЕШЕНИЕ - АВТОИСПРАВЛЕНИЕ
echo ==========================================
echo.

echo Шаг 1: Остановка Gradle daemon...
call gradlew.bat --stop

echo.
echo Шаг 2: Удаление кэшей...
if exist .gradle rmdir /s /q .gradle
if exist app\build rmdir /s /q app\build

echo.
echo Шаг 3: Замена на ультра-минимальные файлы...
if exist gradle.properties ren gradle.properties gradle-old.properties
if exist gradle-ultra-minimal.properties ren gradle-ultra-minimal.properties gradle.properties

if exist app\build.gradle ren app\build.gradle app\build-old.gradle
if exist app\build-ultra-minimal.gradle ren app\build-ultra-minimal.gradle app\build.gradle

echo.
echo Шаг 4: Сборка APK...
call gradlew.bat clean
call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo           УСПЕХ! APK СОЗДАН!
    echo ==========================================
    echo.
    echo APK файл находится в:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Скопируйте этот файл на Android устройство
    echo и установите его.
    echo.
) else (
    echo.
    echo ==========================================
    echo              ОШИБКА СБОРКИ
    echo ==========================================
    echo.
    echo Попробуйте:
    echo 1. Установить JDK 17
    echo 2. Обновить Android Studio
    echo 3. Обратиться к разработчику
    echo.
)

pause