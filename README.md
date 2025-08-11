# Marketplace Microservice Application

Микросервисное приложение маркетплейса на Java 17 и Spring Boot с трехслойной архитектурой.

## 📌 Архитектура

Приложение состоит из следующих сервисов:

- **API Gateway** - маршрутизация запросов к сервисам
- **Auth Service** - регистрация, авторизация, выдача JWT
- **Eureka Server** - Service Discovery для регистрации сервисов
- **Payment Service** - обработка денежных транзакций
- **Order Service** - управление заказами
- **Product Service** - управление продуктами
- **JwtSecurityLib** - общая библиотека для работы с JWT

## 🛠 Технологический стек

- **Язык**: Java 17
- **Фреймворк**: Spring Boot 3
- **Базы данных**:
    - PostgreSQL - основное хранилище
    - KeyDB - кэширование
- **Брокер сообщений**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Документация**: Swagger (OpenAPI 3.0)
- **Тестирование**: JUnit, Mockito, Testcontainers
- **Сборка**: Maven
- **CI/CD**: Jenkins
- **Deploy**: Kubernetes, Helm

## 🏗 Архитектурные особенности

- Трехслойная архитектура с use-case'ами:
    1. **Presentation Layer** (контроллеры)
    2. **Business Logic Layer** (use-cases, сервисы)
    3. **Data Access Layer** (репозитории)
- JWT-аутентификация между сервисами
- Асинхронная коммуникация через Kafka
- Кэширование данных в KeyDB
- Синхронная коммуникация через REST и GRPC
- Микросервисная архитектура

## 🚀 Запуск приложения

### Docker Compose
1. Необходим Docker
2. В корне проекта запускаем:
```docker-compose up --build```
3. Приложение будет доступно по localhost:8080 (ApiGateway)

### Kubernetes (Minikube + Helm)
1. Убедитесь, что у вас установлены Minikube (Или аналоги для Kubernetes) и Helm
2. Предварительно нужно запушить на свой Docker Hub все образы, а в marketplace-app/values.yaml указать свой ник (global.docker.nickname)
3. Запускаем кластер Kubernetes, например с помощью minikube: ```minikube start --driver=docker --memory=4096 --cpus=2```
4. Переходим в директорию чартов: ```cd marketplace-app``` 
5. Обновляем зависимости чартов: ```helm dependency update```
6. Разворачиваем Helm Chart с нашими сервисами в качестве субчартов: ```helm upgrade --install marketplace-app ./ -f values.yaml```


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
     