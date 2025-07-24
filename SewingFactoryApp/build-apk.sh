#!/bin/bash

echo "=========================================="
echo "  Сборка APK для Швейного производства"
echo "=========================================="
echo

# Проверка наличия gradlew
if [ ! -f "./gradlew" ]; then
    echo "ОШИБКА: gradlew не найден!"
    echo "Убедитесь, что вы находитесь в корневой папке проекта."
    exit 1
fi

# Делаем gradlew исполняемым
chmod +x ./gradlew

echo "Очистка предыдущих сборок..."
./gradlew clean

echo
echo "Сборка Debug APK..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo
    echo "ОШИБКА: Не удалось собрать Debug APK!"
    exit 1
fi

echo
echo "Сборка Release APK..."
./gradlew assembleRelease

if [ $? -ne 0 ]; then
    echo
    echo "ПРЕДУПРЕЖДЕНИЕ: Не удалось собрать Release APK (возможно, нет подписи)"
    echo "Но Debug APK собран успешно!"
fi

echo
echo "=========================================="
echo "            СБОРКА ЗАВЕРШЕНА!"
echo "=========================================="
echo
echo "APK файлы находятся в:"
echo "Debug:   app/build/outputs/apk/debug/app-debug.apk"
echo "Release: app/build/outputs/apk/release/app-release-unsigned.apk"
echo
echo "Для установки на устройство используйте Debug версию."
echo