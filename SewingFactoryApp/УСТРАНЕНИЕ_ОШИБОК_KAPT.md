# 🔧 Устранение ошибок kapt и Gradle

## 🚨 Если возникают ошибки типа IllegalAccessError или проблемы с kapt

### Проблема
При сборке проекта могут возникать ошибки вида:
```
java.lang.IllegalAccessError: class kotlin.reflect.jvm.internal.KClassImpl$Data$declaredNonStaticMembers$2 
cannot access a member of class com.sun.tools.javac.code.Symbol$CompletionFailure with modifiers "public"
```

Это происходит из-за ограничений модульной системы Java 9+ при работе с kapt (Kotlin Annotation Processing Tool).

## ✅ Решение 1: Использование исправленного gradle.properties

Файл `gradle.properties` уже содержит все необходимые исправления:

1. **Экспорт пакетов компилятора Java** (`--add-exports`)
2. **Открытие доступа к внутренним пакетам** (`--add-opens`)
3. **Принудительный запуск Kotlin компилятора в том же процессе** (`kotlin.compiler.execution.strategy=in-process`)

### Что делать:
1. **Синхронизируйте проект**: нажмите кнопку "Sync Project with Gradle Files" (иконка слона)
2. **Очистите проект**: Build → Clean Project
3. **Пересоберите**: Build → Rebuild Project

## ✅ Решение 2: Использование стабильной версии

Если проблемы продолжаются, используйте стабильную версию:

1. **Переименуйте** `app/build.gradle` в `app/build-latest.gradle`
2. **Переименуйте** `app/build-stable.gradle` в `app/build.gradle`
3. **Синхронизируйте проект**

Стабильная версия использует:
- Android SDK 33 вместо 34
- Java 11 вместо 8
- Более старые и проверенные версии библиотек

## ✅ Решение 3: Альтернативные настройки JVM

Если проблемы все еще есть, попробуйте эти настройки в `gradle.properties`:

```properties
# Минимальные настройки для JDK 17+
org.gradle.jvmargs=-Xmx3072m -Dfile.encoding=UTF-8 \
  --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
  --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED \
  --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
  --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
  --add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED \
  --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
  --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
  --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
  --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED

# Отключение параллельной сборки для избежания конфликтов
org.gradle.parallel=false
kotlin.compiler.execution.strategy=in-process
kapt.use.worker.api=false
```

## ✅ Решение 4: Использование JDK 11

Если у вас JDK 17+, попробуйте переключиться на JDK 11:

### В Android Studio:
1. File → Project Structure
2. SDK Location → Gradle Settings
3. Gradle JDK → выберите JDK 11

### Локально:
```bash
# Установите JAVA_HOME на JDK 11
export JAVA_HOME=/path/to/jdk11
```

## ✅ Решение 5: Полная очистка

Если ничего не помогает:

1. **Остановите Gradle Daemon**:
   ```bash
   ./gradlew --stop
   ```

2. **Очистите кэши**:
   ```bash
   ./gradlew clean
   rm -rf .gradle
   rm -rf app/build
   ```

3. **В Android Studio**:
   - File → Invalidate Caches and Restart
   - Build → Clean Project
   - Build → Rebuild Project

## 🔍 Диагностика проблем

### Проверьте версию JDK:
```bash
java -version
javac -version
```

### Проверьте переменные среды:
```bash
echo $JAVA_HOME
echo $PATH
```

### Запустите сборку с подробным логом:
```bash
./gradlew assembleDebug --info --stacktrace
```

## 🎯 Рекомендуемая конфигурация

### Для максимальной совместимости:
- **JDK**: 11 (не 8, не 17+)
- **Gradle**: 8.0
- **Android Gradle Plugin**: 8.1.0
- **Kotlin**: 1.8.20
- **compileSdk**: 33

### Настройки gradle.properties:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
kotlin.compiler.execution.strategy=in-process
kapt.use.worker.api=false
kapt.incremental.apt=false
org.gradle.parallel=false
```

## 🚀 Быстрое решение

**Если спешите и нужно просто собрать APK:**

1. Используйте **JDK 11**
2. Замените `build.gradle` на `build-stable.gradle`
3. Выполните:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

## 📞 Если ничего не помогает

1. **Проверьте версии**:
   - Android Studio: последняя стабильная
   - JDK: 11 (рекомендуется)
   - Gradle: автоматически подберется

2. **Создайте новый проект** и перенесите код по частям

3. **Обратитесь к разработчику** с полным логом ошибки

## 💡 Профилактика

Чтобы избежать подобных проблем в будущем:
- Используйте LTS версии JDK (8, 11, 17)
- Не смешивайте разные версии Android SDK
- Регулярно обновляйте Android Studio
- Используйте стабильные версии библиотек в продакшене