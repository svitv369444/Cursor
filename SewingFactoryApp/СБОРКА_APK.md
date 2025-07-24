# 📱 Инструкция по сборке APK файла

## 🎯 Варианты сборки APK

### ✅ Вариант 1: Автоматическая сборка (самый простой)

**Для Windows:**
1. Откройте папку проекта `SewingFactoryApp`
2. Дважды кликните на файл `build-apk.bat`
3. Дождитесь завершения сборки
4. APK файл будет в папке: `app\build\outputs\apk\debug\app-debug.apk`

**Для Linux/Mac:**
1. Откройте терминал в папке проекта `SewingFactoryApp`
2. Выполните команду: `chmod +x build-apk.sh`
3. Запустите: `./build-apk.sh`
4. APK файл будет в папке: `app/build/outputs/apk/debug/app-debug.apk`

---

### ✅ Вариант 2: Через Android Studio

#### Шаг 1: Подготовка
1. **Скачайте и установите Android Studio** с https://developer.android.com/studio
2. **Установите JDK 11 или 17** если его нет
3. **Откройте проект** в Android Studio

#### Шаг 2: Сборка Debug APK (для тестирования)
1. В меню выберите: **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. Дождитесь завершения сборки
3. Нажмите **"locate"** в уведомлении или найдите файл в:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

#### Шаг 3: Сборка Release APK (для продакшена)
1. В меню: **Build** → **Generate Signed Bundle / APK**
2. Выберите **APK**
3. **Создайте keystore** (файл подписи):
   - Key store path: `C:\sewing-factory-key.jks` (или любой путь)
   - Password: придумайте надежный пароль
   - Key alias: `sewingfactory`
   - Key password: пароль для ключа
   - Validity: 25 лет
   - Certificate info: заполните данные организации
4. Выберите **release** build variant
5. Нажмите **Finish**

---

### ✅ Вариант 3: Через командную строку

#### Подготовка:
```bash
# Перейдите в папку проекта
cd SewingFactoryApp

# Сделайте gradlew исполняемым (только для Linux/Mac)
chmod +x gradlew
```

#### Сборка Debug APK:
```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

#### Сборка Release APK:
```bash
# Windows
gradlew.bat assembleRelease

# Linux/Mac
./gradlew assembleRelease
```

---

## 📁 Где найти готовые APK файлы

После сборки APK файлы будут находиться в:

```
SewingFactoryApp/
└── app/
    └── build/
        └── outputs/
            └── apk/
                ├── debug/
                │   └── app-debug.apk          ← Для тестирования
                └── release/
                    └── app-release-unsigned.apk  ← Неподписанная версия
                    └── app-release.apk           ← Подписанная версия
```

---

## 🔧 Устранение проблем

### ❌ Ошибка: "JAVA_HOME не установлен"
**Решение:**
1. Установите JDK 11 или 17
2. Добавьте в переменные среды:
   - `JAVA_HOME` = путь к JDK (например: `C:\Program Files\Java\jdk-11.0.16`)
   - Добавьте в `PATH`: `%JAVA_HOME%\bin`

### ❌ Ошибка: "Android SDK не найден"
**Решение:**
1. Установите Android Studio
2. Откройте SDK Manager и установите:
   - Android SDK Platform 34
   - Android SDK Build-Tools
   - Android SDK Platform-Tools

### ❌ Ошибка: "gradlew не найден"
**Решение:**
1. Убедитесь, что находитесь в папке `SewingFactoryApp`
2. Проверьте наличие файлов `gradlew` и `gradlew.bat`

### ❌ Ошибка: "Permission denied" (Linux/Mac)
**Решение:**
```bash
chmod +x gradlew
chmod +x build-apk.sh
```

---

## 🚀 Быстрый старт для новичков

### Если у вас нет опыта:

1. **Скачайте Android Studio** с официального сайта
2. **Установите** его со всеми компонентами по умолчанию
3. **Откройте проект** `SewingFactoryApp`
4. **Дождитесь** синхронизации (может занять 10-15 минут)
5. **Нажмите** зеленую кнопку ▶️ **Run** в верхней панели
6. **Выберите** "Build APK(s)" из выпадающего меню
7. **Готово!** APK файл создан

---

## 📋 Системные требования

### Для сборки APK:
- **ОС**: Windows 10+, macOS 10.14+, Ubuntu 18.04+
- **RAM**: минимум 4 ГБ (рекомендуется 8 ГБ)
- **Место на диске**: 10 ГБ свободного места
- **Java**: JDK 11 или 17
- **Интернет**: для загрузки зависимостей

### Для установки APK:
- **Android**: 7.0 (API 24) или выше
- **Место**: 50 МБ свободного места
- **Разрешения**: Камера, Интернет

---

## 🎉 Что делать с готовым APK

1. **Скопируйте** `app-debug.apk` на Android устройство
2. **Включите** "Установка из неизвестных источников" в настройках
3. **Найдите** APK файл в файловом менеджере
4. **Нажмите** на файл и выберите "Установить"
5. **Запустите** приложение "Швейное производство"

---

## 📞 Поддержка

Если возникли проблемы:
1. Проверьте этот файл еще раз
2. Убедитесь, что выполнили все требования
3. Попробуйте перезагрузить компьютер
4. Обратитесь к IT-специалисту с этой инструкцией