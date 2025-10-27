#!/bin/bash

# Скрипт для загрузки Google AI Edge Gallery APK
# Версия: 1.0

set -e

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_header "ЗАГРУЗКА GOOGLE AI EDGE GALLERY"

# Проверка наличия curl или wget
if command -v curl &> /dev/null; then
    DOWNLOADER="curl"
    print_message "Используем curl для загрузки"
elif command -v wget &> /dev/null; then
    DOWNLOADER="wget"
    print_message "Используем wget для загрузки"
else
    print_error "Не найден curl или wget. Установите один из них для автоматической загрузки."
    echo
    print_message "Альтернативный способ:"
    print_message "1. Откройте браузер"
    print_message "2. Перейдите на: https://github.com/google-ai-edge/ai-edge-gallery"
    print_message "3. Нажмите 'Releases' → скачайте последний .apk файл"
    exit 1
fi

# Создание папки для загрузки
mkdir -p downloads
cd downloads

print_message "Получение информации о последней версии..."

# GitHub API для получения последнего релиза
API_URL="https://api.github.com/repos/google-ai-edge/ai-edge-gallery/releases/latest"

if [ "$DOWNLOADER" = "curl" ]; then
    RELEASE_INFO=$(curl -s "$API_URL")
else
    RELEASE_INFO=$(wget -qO- "$API_URL")
fi

# Попытка найти APK файл в релизе
APK_URL=$(echo "$RELEASE_INFO" | grep -o '"browser_download_url": "[^"]*\.apk"' | head -1 | cut -d'"' -f4)

if [ -z "$APK_URL" ]; then
    print_warning "Не удалось найти APK файл автоматически"
    echo
    print_message "Попробуйте загрузить вручную:"
    print_message "1. Перейдите на: https://github.com/google-ai-edge/ai-edge-gallery/releases"
    print_message "2. Найдите последний релиз"
    print_message "3. Скачайте файл с расширением .apk"
    
    # Альтернативная попытка
    print_message ""
    print_message "Альтернативно, проверьте эти возможные ссылки:"
    echo "$RELEASE_INFO" | grep -o '"browser_download_url": "[^"]*"' | cut -d'"' -f4 | head -5
    
    exit 1
fi

print_message "Найден APK файл: $APK_URL"

# Получение имени файла
APK_FILENAME=$(basename "$APK_URL")
print_message "Имя файла: $APK_FILENAME"

# Проверка, если файл уже существует
if [ -f "$APK_FILENAME" ]; then
    print_warning "Файл $APK_FILENAME уже существует"
    read -p "Перезагрузить? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_message "Используем существующий файл"
        APK_PATH="$(pwd)/$APK_FILENAME"
    else
        rm -f "$APK_FILENAME"
    fi
fi

# Загрузка APK файла
if [ ! -f "$APK_FILENAME" ]; then
    print_message "Загружаем $APK_FILENAME..."
    
    if [ "$DOWNLOADER" = "curl" ]; then
        curl -L -o "$APK_FILENAME" "$APK_URL" --progress-bar
    else
        wget -O "$APK_FILENAME" "$APK_URL" --progress=bar:force
    fi
    
    APK_PATH="$(pwd)/$APK_FILENAME"
fi

# Проверка загрузки
if [ -f "$APK_FILENAME" ]; then
    FILE_SIZE=$(ls -lh "$APK_FILENAME" | awk '{print $5}')
    print_message "✅ Загрузка завершена успешно!"
    print_message "Размер файла: $FILE_SIZE"
    print_message "Путь к файлу: $APK_PATH"
else
    print_error "❌ Ошибка загрузки файла"
    exit 1
fi

cd ..

print_header "СЛЕДУЮЩИЕ ШАГИ"

print_message "APK файл загружен в: downloads/$APK_FILENAME"
echo
print_message "Теперь выполните следующие действия:"
echo
echo "1. 📱 ПОДГОТОВКА ANDROID УСТРОЙСТВА:"
echo "   • Откройте Настройки → Безопасность"
echo "   • Включите 'Установка из неизвестных источников'"
echo
echo "2. 📂 ПЕРЕДАЧА ФАЙЛА НА ANDROID:"
echo "   • Скопируйте файл downloads/$APK_FILENAME на Android устройство"
echo "   • Или используйте ADB: adb install downloads/$APK_FILENAME"
echo
echo "3. 🔨 УСТАНОВКА:"
echo "   • Откройте файловый менеджер на Android"
echo "   • Найдите и нажмите на APK файл"
echo "   • Подтвердите установку"
echo
echo "4. 🚀 ЗАПУСК:"
echo "   • Найдите 'AI Edge Gallery' в списке приложений"
echo "   • Запустите приложение"
echo "   • Выберите 'AI Chat' и загрузите модель Gemma 3"

print_header "АЛЬТЕРНАТИВНЫЕ МЕТОДЫ УСТАНОВКИ"

echo "Если возникли проблемы с загрузкой:"
echo
echo "🔧 Автоматическая установка через ADB:"
echo "   ./android_setup_script.sh"
echo
echo "📱 Альтернативные приложения:"
echo "   • PocketPal AI (Google Play Store)"
echo "   • MLC Chat (GitHub releases)"
echo
echo "📖 Подробные инструкции:"
echo "   • Читайте: ai_edge_gallery_setup.md"
echo "   • Или: gemma3_android_deployment_guide.md"

print_message "Удачной установки! 🎉"