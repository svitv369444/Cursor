#!/bin/bash

# Скрипт для настройки развертывания Gemma 3 на Android
# Автор: AI Assistant
# Версия: 1.0

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для вывода сообщений
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

# Проверка ADB
check_adb() {
    if ! command -v adb &> /dev/null; then
        print_error "ADB не установлен. Установите Android SDK Platform Tools."
        print_message "Скачайте с: https://developer.android.com/studio/releases/platform-tools"
        exit 1
    fi
    print_message "ADB найден: $(adb version | head -n1)"
}

# Проверка подключения устройства
check_device() {
    devices=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)
    if [ "$devices" -eq 0 ]; then
        print_error "Android устройство не подключено или не авторизовано."
        print_message "Подключите устройство и включите отладку по USB."
        exit 1
    fi
    print_message "Найдено Android устройств: $devices"
}

# Получение информации об устройстве
get_device_info() {
    print_header "ИНФОРМАЦИЯ ОБ УСТРОЙСТВЕ"
    
    MANUFACTURER=$(adb shell getprop ro.product.manufacturer)
    MODEL=$(adb shell getprop ro.product.model)
    ANDROID_VERSION=$(adb shell getprop ro.build.version.release)
    API_LEVEL=$(adb shell getprop ro.build.version.sdk)
    CPU_ABI=$(adb shell getprop ro.product.cpu.abi)
    
    print_message "Производитель: $MANUFACTURER"
    print_message "Модель: $MODEL"
    print_message "Android версия: $ANDROID_VERSION (API $API_LEVEL)"
    print_message "CPU архитектура: $CPU_ABI"
    
    # Проверка RAM
    RAM_INFO=$(adb shell cat /proc/meminfo | grep MemTotal | awk '{print $2}')
    RAM_GB=$((RAM_INFO / 1024 / 1024))
    print_message "RAM: ${RAM_GB} ГБ"
    
    # Рекомендации по модели
    if [ "$RAM_GB" -lt 4 ]; then
        print_warning "Мало RAM для запуска Gemma 3. Рекомендуется минимум 4 ГБ."
    elif [ "$RAM_GB" -lt 8 ]; then
        print_message "Рекомендуется использовать Gemma 3 2B модель."
    elif [ "$RAM_GB" -lt 12 ]; then
        print_message "Можно использовать Gemma 3 9B модель."
    else
        print_message "Можно использовать любую модель Gemma 3, включая 27B."
    fi
    
    # Проверка версии Android
    if [ "$API_LEVEL" -lt 31 ]; then
        print_warning "Рекомендуется Android 12+ (API 31+) для лучшей производительности."
    fi
}

# Скачивание APK файлов
download_apks() {
    print_header "СКАЧИВАНИЕ APK ФАЙЛОВ"
    
    mkdir -p apks
    cd apks
    
    # Google AI Edge Gallery
    print_message "Скачивание Google AI Edge Gallery..."
    if [ ! -f "ai-edge-gallery.apk" ]; then
        print_message "Перейдите на https://github.com/google-ai-edge/ai-edge-gallery"
        print_message "Скачайте последний APK и поместите его в папку apks/ как ai-edge-gallery.apk"
    fi
    
    # PocketPal AI (через Google Play или APK)
    print_message "PocketPal AI можно установить из Google Play Store"
    print_message "Ссылка: https://play.google.com/store/apps/details?id=com.pocketpal.ai"
    
    cd ..
}

# Установка приложений
install_apps() {
    print_header "УСТАНОВКА ПРИЛОЖЕНИЙ"
    
    # Проверка разрешений на установку
    print_message "Проверка разрешений на установку приложений..."
    
    if [ -f "apks/ai-edge-gallery.apk" ]; then
        print_message "Установка Google AI Edge Gallery..."
        adb install apks/ai-edge-gallery.apk || print_warning "Не удалось установить AI Edge Gallery"
    else
        print_warning "APK файл ai-edge-gallery.apk не найден"
    fi
    
    # MLC Chat - предоставляем ссылку для скачивания
    print_message "Для установки MLC Chat:"
    print_message "1. Перейдите на https://github.com/mlc-ai/mlc-llm/releases"
    print_message "2. Скачайте MLCChat-*.apk"
    print_message "3. Установите через adb install MLCChat-*.apk"
}

# Проверка установленных приложений
check_installed_apps() {
    print_header "ПРОВЕРКА УСТАНОВЛЕННЫХ ПРИЛОЖЕНИЙ"
    
    # Google AI Edge Gallery
    if adb shell pm list packages | grep -q "ai.edge.gallery"; then
        print_message "✓ Google AI Edge Gallery установлен"
    else
        print_warning "✗ Google AI Edge Gallery не установлен"
    fi
    
    # PocketPal AI
    if adb shell pm list packages | grep -q "com.pocketpal.ai"; then
        print_message "✓ PocketPal AI установлен"
    else
        print_warning "✗ PocketPal AI не установлен"
    fi
    
    # MLC Chat
    if adb shell pm list packages | grep -q "ai.mlc.mlcchat"; then
        print_message "✓ MLC Chat установлен"
    else
        print_warning "✗ MLC Chat не установлен"
    fi
}

# Настройка производительности
optimize_performance() {
    print_header "ОПТИМИЗАЦИЯ ПРОИЗВОДИТЕЛЬНОСТИ"
    
    print_message "Применение настроек производительности..."
    
    # Включение режима производительности (требует root)
    print_message "Попытка включения режима производительности..."
    adb shell "echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" 2>/dev/null || print_warning "Не удалось изменить губернатор CPU (требует root)"
    
    # Отключение энергосбережения для GPU
    adb shell "echo 0 > /sys/class/kgsl/kgsl-3d0/devfreq/adrenoboost" 2>/dev/null || print_warning "Не удалось настроить GPU (требует root или специфично для устройства)"
    
    # Очистка кэша
    print_message "Очистка кэша системы..."
    adb shell "sync; echo 3 > /proc/sys/vm/drop_caches" 2>/dev/null || print_warning "Не удалось очистить кэш (требует root)"
    
    print_message "Рекомендации для оптимизации:"
    print_message "1. Закройте все ненужные приложения"
    print_message "2. Подключите устройство к зарядке"
    print_message "3. Включите режим производительности в настройках"
    print_message "4. Убедитесь, что устройство не перегревается"
}

# Создание файла с рекомендациями
create_recommendations() {
    print_header "СОЗДАНИЕ ФАЙЛА РЕКОМЕНДАЦИЙ"
    
    cat > gemma3_recommendations.txt << EOF
РЕКОМЕНДАЦИИ ПО ЗАПУСКУ GEMMA 3 НА ANDROID

Основываясь на вашем устройстве:
- Производитель: $MANUFACTURER
- Модель: $MODEL  
- Android: $ANDROID_VERSION (API $API_LEVEL)
- RAM: ${RAM_GB} ГБ
- CPU: $CPU_ABI

РЕКОМЕНДУЕМЫЕ МОДЕЛИ:
$(if [ "$RAM_GB" -lt 4 ]; then
    echo "- Модель слишком мала для Gemma 3. Рассмотрите облачные решения."
elif [ "$RAM_GB" -lt 8 ]; then
    echo "- Gemma 3 2B (квантованная 4-bit версия)"
    echo "- Gemma 3n 2B для мобильных устройств"
elif [ "$RAM_GB" -lt 12 ]; then
    echo "- Gemma 3 2B или 9B"
    echo "- Рекомендуется квантованная версия для 9B"
else
    echo "- Любая модель Gemma 3 (2B, 9B, 27B)"
    echo "- Для лучшей производительности используйте квантованные версии"
fi)

РЕКОМЕНДУЕМЫЕ ПРИЛОЖЕНИЯ:
1. Google AI Edge Gallery - простота использования
2. PocketPal AI - легкий и быстрый
3. MLC Chat - максимальная производительность

СЛЕДУЮЩИЕ ШАГИ:
1. Установите одно из рекомендованных приложений
2. Загрузите подходящую модель
3. Протестируйте производительность
4. При необходимости переключитесь на меньшую модель

УСТРАНЕНИЕ ПРОБЛЕМ:
- Если приложение вылетает: используйте меньшую модель
- Если генерация медленная: включите GPU ускорение
- Если не хватает места: удалите ненужные файлы

Дата создания: $(date)
EOF

    print_message "Рекомендации сохранены в gemma3_recommendations.txt"
}

# Главная функция
main() {
    print_header "НАСТРОЙКА GEMMA 3 ДЛЯ ANDROID"
    print_message "Этот скрипт поможет настроить Gemma 3 на вашем Android устройстве"
    
    echo
    read -p "Продолжить? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_message "Отменено пользователем."
        exit 0
    fi
    
    check_adb
    check_device
    get_device_info
    download_apks
    install_apps
    check_installed_apps
    optimize_performance
    create_recommendations
    
    print_header "НАСТРОЙКА ЗАВЕРШЕНА"
    print_message "Проверьте файл gemma3_recommendations.txt для персональных рекомендаций"
    print_message "Теперь вы можете запустить одно из установленных приложений и загрузить модель Gemma 3"
}

# Обработка аргументов командной строки
case "${1:-}" in
    --device-info)
        check_adb
        check_device
        get_device_info
        ;;
    --install-only)
        check_adb
        check_device
        install_apps
        ;;
    --optimize)
        check_adb
        check_device
        optimize_performance
        ;;
    --help|-h)
        echo "Использование: $0 [ОПЦИЯ]"
        echo ""
        echo "ОПЦИИ:"
        echo "  --device-info    Показать только информацию об устройстве"
        echo "  --install-only   Только установка приложений"
        echo "  --optimize       Только оптимизация производительности"
        echo "  --help, -h       Показать эту справку"
        echo ""
        echo "Без аргументов: выполнить полную настройку"
        ;;
    *)
        main
        ;;
esac