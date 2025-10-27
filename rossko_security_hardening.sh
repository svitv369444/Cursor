#!/bin/bash

# СКРИПТ УСИЛЕНИЯ БЕЗОПАСНОСТИ omsk.rossko.ru
# Выполнять ПОСЛЕ восстановления работоспособности

echo "🛡️ УСИЛЕНИЕ БЕЗОПАСНОСТИ САЙТА omsk.rossko.ru"
echo "================================================"

# 1. ОБНОВЛЕНИЕ СИСТЕМЫ
echo "📦 ШАГ 1: Обновление системы и пакетов..."
apt update && apt upgrade -y

# 2. УСТАНОВКА ЗАЩИТЫ ОТ DDOS
echo -e "\n🚫 ШАГ 2: Настройка защиты от DDoS..."

# Создаем конфигурацию nginx для защиты от DDoS
cat > /etc/nginx/conf.d/ddos-protection.conf << 'EOF'
# Защита от DDoS-атак
limit_req_zone $binary_remote_addr zone=login:10m rate=1r/s;
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
limit_req_zone $binary_remote_addr zone=general:10m rate=5r/s;
limit_conn_zone $binary_remote_addr zone=addr:10m;

# Блокировка подозрительных User-Agent
map $http_user_agent $bad_bot {
    default 0;
    ~*curl 1;
    ~*wget 1;
    ~*python 1;
    ~*scanner 1;
    ~*bot 1;
    ~*spider 1;
    ~*crawler 1;
}

# Блокировка по странам (опционально)
geo $blocked_country {
    default 0;
    # Раскомментируйте нужные страны
    # CN 1; # Китай
    # RU 0; # Россия - разрешить
}
EOF

# 3. УСИЛЕНИЕ КОНФИГУРАЦИИ NGINX
echo -e "\n⚙️ ШАГ 3: Усиление конфигурации веб-сервера..."

# Добавляем безопасные заголовки в nginx
cat > /etc/nginx/conf.d/security-headers.conf << 'EOF'
# Заголовки безопасности
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' https:; connect-src 'self'; frame-ancestors 'self';" always;

# Скрытие версии nginx
server_tokens off;

# Защита от атак
client_max_body_size 10M;
client_body_timeout 60s;
client_header_timeout 60s;
keepalive_timeout 15;
send_timeout 60s;
EOF

# 4. НАСТРОЙКА FAIL2BAN
echo -e "\n🔒 ШАГ 4: Установка и настройка Fail2Ban..."
apt install -y fail2ban

cat > /etc/fail2ban/jail.local << 'EOF'
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 5
ignoreip = 127.0.0.1/8

[nginx-http-auth]
enabled = true
port = http,https
logpath = /var/log/nginx/error.log

[nginx-noscript]
enabled = true
port = http,https
logpath = /var/log/nginx/access.log
maxretry = 6

[nginx-badbots]
enabled = true
port = http,https
logpath = /var/log/nginx/access.log
maxretry = 2

[nginx-noproxy]
enabled = true
port = http,https
logpath = /var/log/nginx/access.log
maxretry = 2
EOF

# 5. БЕЗОПАСНОСТЬ MySQL
echo -e "\n🗄️ ШАГ 5: Усиление безопасности MySQL..."

# Изменяем стандартные настройки MySQL
cat >> /etc/mysql/mysql.conf.d/security.cnf << 'EOF'
[mysqld]
# Отключение небезопасных функций
local-infile = 0
skip-show-database
safe-user-create = 1

# Сетевая безопасность
bind-address = 127.0.0.1

# Логирование
log-error = /var/log/mysql/error.log
general_log = 1
general_log_file = /var/log/mysql/general.log
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
EOF

# 6. СОЗДАНИЕ РЕЗЕРВНОЙ КОПИИ
echo -e "\n💾 ШАГ 6: Создание резервной копии..."
mkdir -p /backups/$(date +%Y%m%d)

# Бэкап файлов сайта
tar -czf /backups/$(date +%Y%m%d)/website_backup.tar.gz /var/www/html/ 2>/dev/null

# Бэкап базы данных (укажите правильное имя БД)
mysqldump --all-databases > /backups/$(date +%Y%m%d)/database_backup.sql 2>/dev/null

# 7. НАСТРОЙКА МОНИТОРИНГА
echo -e "\n📊 ШАГ 7: Настройка мониторинга..."

# Создаем скрипт мониторинга
cat > /usr/local/bin/site_monitor.sh << 'EOF'
#!/bin/bash
LOG_FILE="/var/log/site_monitor.log"
SITE_URL="https://omsk.rossko.ru"

# Проверка доступности сайта
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 $SITE_URL)

if [ "$HTTP_CODE" != "200" ]; then
    echo "$(date): КРИТИЧНО! Сайт недоступен. HTTP код: $HTTP_CODE" >> $LOG_FILE
    # Здесь можно добавить отправку уведомления
else
    echo "$(date): OK - Сайт доступен" >> $LOG_FILE
fi
EOF

chmod +x /usr/local/bin/site_monitor.sh

# Добавляем в cron для проверки каждые 5 минут
(crontab -l 2>/dev/null; echo "*/5 * * * * /usr/local/bin/site_monitor.sh") | crontab -

# 8. ПЕРЕЗАПУСК СЕРВИСОВ
echo -e "\n🔄 ШАГ 8: Применение настроек..."
systemctl restart nginx
systemctl restart mysql
systemctl enable fail2ban
systemctl start fail2ban

echo -e "\n================================================"
echo "✅ УСИЛЕНИЕ БЕЗОПАСНОСТИ ЗАВЕРШЕНО!"
echo "📊 Проверьте статус защиты:"
echo "   - fail2ban-client status"
echo "   - systemctl status nginx"
echo "   - tail -f /var/log/site_monitor.log"
echo "================================================"