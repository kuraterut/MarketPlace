# Marketplace Microservice Application

Микросервисное приложение маркетплейса на Java 17 и Spring Boot с трехслойной архитектурой.

## 📌 Архитектура

![Архитектура приложения](docs/architecture.png) *(замените на реальную диаграмму)*

Приложение состоит из следующих сервисов:

- **API Gateway** - маршрутизация запросов к сервисам
- **Auth Service** - регистрация, авторизация, выдача JWT
- **Eureka Server** - Service Discovery для регистрации сервисов
- **Bank Account Service** - управление счетами пользователей
- **Payment Service** - обработка денежных транзакций
- **Order Service** - управление заказами
- **Product Service** - управление продуктами
- **JwtSecurityLib** - общая библиотека для работы с JWT

## 🛠 Технологический стек

- **Язык**: Java 17
- **Фреймворк**: Spring Boot 3.x
- **Базы данных**:
    - PostgreSQL - основное хранилище
    - KeyDB - кэширование
- **Брокер сообщений**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Документация**: Swagger (OpenAPI 3.0)
- **Тестирование**: JUnit, Mockito
- **Сборка**: Maven/Gradle

## 🏗 Архитектурные особенности

- Трехслойная архитектура с use-case'ами:
    1. **Presentation Layer** (контроллеры)
    2. **Business Logic Layer** (use-cases, сервисы)
    3. **Data Access Layer** (репозитории)
- JWT-аутентификация между сервисами
- Асинхронная коммуникация через Kafka
- Кэширование данных в KeyDB
- Сервис-ориентированная архитектура

## 🚀 Запуск приложения

1. Убедитесь, что установлены:
    - Docker

2. Запустите инфраструктурные сервисы (Должен быть запущен Docker Daemon):
   ```bash
   docker compose up --build -d
   ```

## 📄 Документация API

- Доступна после запуска сервисов:
    1. **SwaggerUI:** http://<IP:PORT>/swagger-ui.html
    2. **OpenAPI JSON** http://<IP:PORT>/v3/api-docs

## � Тестирование
- Запуск всех тестов
```mvn test```

- Запуск тестов конкретного сервиса
```mvn test -pl <service-name>```

## 🤝 Коммуникация между сервисами
- Синхронная (REST)
  - Через API Gateway с JWT-аутентификацией 
  - Использует FeignClient для service-to-service вызовов

- Асинхронная(Kafka)


## 🔒 Безопасность
- JWT (HS256)
- Роли: SELLER, ADMIN, CUSTOMER
- Шифрование чувствительных данных (например, баланс счета)
- CORS политики настроены в API Gateway
     