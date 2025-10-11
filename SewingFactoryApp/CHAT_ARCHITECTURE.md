# Архитектура чат-агента

## Схема компонентов

```
MainActivity
    ↓ (кнопка чата)
ChatActivity
    ↓
ChatViewModel ←→ ChatService
    ↓              ↓
ChatMessageDao   ChatContext
    ↓              ↓
Room Database    Worker/WorkTask/Stats
    ↓
ChatMessage

ChatAdapter
    ↓
RecyclerView
    ↓
item_chat_message.xml (пользователь)
item_chat_agent_message.xml (агент)
```

## Поток данных

1. **Пользователь** вводит сообщение в ChatActivity
2. **ChatActivity** передает сообщение в ChatViewModel
3. **ChatViewModel** сохраняет сообщение пользователя в базу данных
4. **ChatViewModel** получает контекст (работник, задание, статистика)
5. **ChatService** обрабатывает сообщение и генерирует ответ агента
6. **ChatViewModel** сохраняет ответ агента в базу данных
7. **ChatAdapter** отображает обновленный список сообщений
8. **RecyclerView** показывает новые сообщения пользователю

## Компоненты

### UI Layer
- **ChatActivity**: Основной экран чата
- **ChatAdapter**: Адаптер для отображения сообщений
- **Layouts**: Макеты для чата и сообщений

### ViewModel Layer
- **ChatViewModel**: Управление состоянием чата
- **MainViewModel**: Предоставляет контекст (работник, задание)

### Service Layer
- **ChatService**: Обработка сообщений и генерация ответов агента

### Data Layer
- **ChatMessageDao**: Работа с базой данных сообщений
- **ChatMessage**: Модель данных сообщения
- **ChatContext**: Контекст для агента

## Интеграция с основным приложением

Чат-агент интегрирован в основное приложение через:
- Кнопку в MainActivity
- Использование общего MainViewModel для контекста
- Общую базу данных Room
- Общие модели данных (Worker, WorkTask, WorkerDayStats)