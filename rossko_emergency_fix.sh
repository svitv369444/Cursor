#!/bin/bash

# СКРИПТ ЭКСТРЕННОГО ВОССТАНОВЛЕНИЯ САЙТА omsk.rossko.ru
# Выполнять с правами root на сервере

echo "🚨 НАЧИНАЕМ ЭКСТРЕННОЕ ВОССТАНОВЛЕНИЕ САЙТА omsk.rossko.ru"
echo "================================================================"

# 1. ДИАГНОСТИКА ТЕКУЩЕГО СОСТОЯНИЯ
echo "📊 ШАГ 1: Диагностика системы..."

echo "Проверка статуса сервисов:"
systemctl status nginx --no-pager
systemctl status angie --no-pager 2>/dev/null || echo "Angie не найден"
systemctl status php8.1-fpm --no-pager 2>/dev/null || systemctl status php-fpm --no-pager
systemctl status mysql --no-pager 2>/dev/null || systemctl status mariadb --no-pager

echo -e "\nПроверка дискового пространства:"
df -h

echo -e "\nПроверка памяти:"
free -h

echo -e "\nПроверка нагрузки:"
uptime

# 2. ПРОВЕРКА ЛОГОВ ОШИБОК
echo -e "\n📋 ШАГ 2: Анализ логов..."

echo "Последние ошибки nginx/angie:"
tail -20 /var/log/nginx/error.log 2>/dev/null || tail -20 /var/log/angie/error.log 2>/dev/null

echo -e "\nПоследние ошибки PHP:"
tail -20 /var/log/php*/error.log 2>/dev/null

echo -e "\nПоследние ошибки MySQL:"
tail -20 /var/log/mysql/error.log 2>/dev/null || tail -20 /var/log/mysqld.log 2>/dev/null

# 3. ЭКСТРЕННЫЙ ПЕРЕЗАПУСК СЕРВИСОВ
echo -e "\n🔄 ШАГ 3: Перезапуск сервисов..."

echo "Останавливаем сервисы..."
systemctl stop nginx 2>/dev/null || systemctl stop angie 2>/dev/null
systemctl stop php8.1-fpm 2>/dev/null || systemctl stop php-fpm 2>/dev/null

sleep 3

echo "Запускаем сервисы..."
systemctl start mysql 2>/dev/null || systemctl start mariadb 2>/dev/null
sleep 2
systemctl start php8.1-fpm 2>/dev/null || systemctl start php-fpm 2>/dev/null
sleep 2
systemctl start nginx 2>/dev/null || systemctl start angie 2>/dev/null

# 4. ПРОВЕРКА РЕЗУЛЬТАТА
echo -e "\n✅ ШАГ 4: Проверка восстановления..."

echo "Статус сервисов после перезапуска:"
systemctl is-active mysql 2>/dev/null || systemctl is-active mariadb 2>/dev/null
systemctl is-active php8.1-fpm 2>/dev/null || systemctl is-active php-fpm 2>/dev/null
systemctl is-active nginx 2>/dev/null || systemctl is-active angie 2>/dev/null

echo -e "\nТестируем доступность сайта:"
curl -I --connect-timeout 10 http://localhost/ 2>/dev/null || echo "Локальный тест не прошел"
curl -I --connect-timeout 10 https://omsk.rossko.ru/ 2>/dev/null || echo "Внешний тест не прошел"

echo -e "\n================================================================"
echo "🎯 РЕЗУЛЬТАТ: Скрипт завершен. Проверьте доступность сайта!"
echo "Если проблема не решена, выполните ручную диагностику."