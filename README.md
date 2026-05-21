# Мультисервисное приложение

> Автор: Мария Игнатова  
> Стек: Java 17 · Spring Boot 3.2.5 · Docker Compose · PostgreSQL · Redis · RabbitMQ · Prometheus · Grafana

---

## О проекте

Монорепозиторий из трёх независимых микросервисов, демонстрирующих:

| Сервис                   | Назначение                | Особенности                                                       |
|--------------------------|---------------------------|-------------------------------------------------------------------|
| **product-service**      | Каталог товаров (CRUD)    | Кеш Redis (Cache-Aside, TTL 10 мин), публикует события в RabbitMQ |
| **order-service**        | Создание и отмена заказов | CQRS + Event Sourcing, потребляет/публикует события               |
| **notification-service** | Уведомления               | Чистый consumer RabbitMQ, метрики Micrometer                      |

Вся инфраструктура (БД, брокер, мониторинг) поднимается одной командой Docker Compose.

---

## Быстрый старт

### Требования
- Docker 24+
- Docker Compose v2 (`docker compose` без дефиса)

### Запуск
```bash
# Клонировать репозиторий
git clone git@github.com:lvrmmm/online-store-microservices.git
cd online-store-microservices

# Собрать образы и запустить все сервисы
docker compose up --build

# Запуск в фоне
docker compose up --build -d
```

### Остановка
```bash
docker compose down          # остановить контейнеры
docker compose down -v       # + удалить тома (данные БД)
```

---

## Порты и веб-интерфейсы

| Сервис / компонент                | URL                                       | Описание                           |
|-----------------------------------|-------------------------------------------|------------------------------------|
| **Product Service**               | http://localhost:8080                     | REST API                           |
| Product Service — Swagger UI      | http://localhost:8080/swagger-ui.html     | Документация и ручное тестирование |
| Product Service — метрики         | http://localhost:8080/actuator/prometheus | Prometheus scrape endpoint         |
| **Order Service**                 | http://localhost:8081                     | REST API                           |
| Order Service — Swagger UI        | http://localhost:8081/swagger-ui.html     | Документация и ручное тестирование |
| Order Service — метрики           | http://localhost:8081/actuator/prometheus | Prometheus scrape endpoint         |
| **Notification Service**          | http://localhost:8082                     | Нет Write-API, только consumer     |
| Notification Service — Swagger UI | http://localhost:8082/swagger-ui.html     |                                    |
| Notification Service — метрики    | http://localhost:8082/actuator/prometheus | Prometheus scrape endpoint         |
| **RabbitMQ Management UI**        | http://localhost:15672                    | guest / guest                      |
| **Prometheus**                    | http://localhost:9090                     | Просмотр метрик, PromQL            |
| **Grafana**                       | http://localhost:3000                     | admin / admin                      |
| **Alertmanager**                  | http://localhost:9093                     | Управление алертами                |
| **Mailpit** (SMTP-заглушка)       | http://localhost:8025                     | Перехваченные письма алертов       |

---

## REST API — краткая справка

### Product Service (`localhost:8080`)

```
POST   /api/products          Создать продукт
GET    /api/products          Список всех продуктов
GET    /api/products/{id}     Получить продукт по UUID (с кешированием)
DELETE /api/products/{id}     Удалить продукт
```

Пример запроса создания продукта:
#### POST /api/products
```json
{
  "name": "Ноутбук Dell XPS 15",
  "description": "Описание",
  "price": 129999.99,
  "stock": 10
}
```

### Order Service (`localhost:8081`)

```
POST   /api/orders                    Создать заказ
POST   /api/orders/{orderId}/cancel   Отменить заказ
GET    /api/orders                    Список всех заказов
GET    /api/orders/{orderId}          Получить заказ по UUID
GET    /api/orders/{orderId}/history  Полная история событий (Event Store)
```

Пример запроса создания заказа:
#### POST /api/orders
```json
{
  "productId": "<UUID продукта>",
  "quantity": 2,
  "pricePerUnit": 129999.99
}
```

---

## Структура монорепозитория

```
online-store-microservices/
├── docker-compose.yml
├── prometheus.yml
├── alert_rules.yml
├── alertmanager.yml
├── pom.xml                          ← корневой Maven POM (multi-module)
├── grafana/                         
│   ├── provisioning/
│   │   ├── datasources/
│   │   │   └── prometheus.yml       
│   │   └── dashboards/
│   │       └── default.yml          
│   └── dashboards/                  
│       └── *.json
└── services/
    ├── product-service/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/
    ├── order-service/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/
    └── notification-service/
        ├── Dockerfile
        ├── pom.xml
        └── src/
```

---

## Как проверить работу сервисов

1. **Создать продукт** → Swagger UI product-service → POST /api/products → скопировать `id`
2. **Создать заказ** → Swagger UI order-service → POST /api/orders (вставить `productId`) → скопировать `orderId`
3. **Проверить уведомление** → `docker logs notification-service` → в логах строка `[NOTIFICATION] New order: ...`
4. **Отменить заказ** → POST /api/orders/{orderId}/cancel → 204
5. **Посмотреть историю** → GET /api/orders/{orderId}/history → два события: Created + Cancelled
6. **Открыть Grafana** → http://localhost:3000 → дашборды 

---

## Переменные окружения (docker-compose.yml)

Все переменные вынесены в блок `environment` каждого сервиса. Для смены паролей БД или RabbitMQ достаточно отредактировать docker-compose.yml:

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://product-db:5432/product-db
SPRING_DATA_REDIS_HOST: product-redis
SPRING_RABBITMQ_HOST: rabbitmq
```

---

## Технологии

| Технология        | Версия          | Роль                   |
|-------------------|-----------------|------------------------|
| Java              | 17              | Язык разработки        |
| Spring Boot       | 3.2.5           | Фреймворк              |
| PostgreSQL        | 16-alpine       | РСУБД                  |
| Redis             | 7-alpine        | Кеш                    |
| RabbitMQ          | 3.13-management | Брокер сообщений       |
| Docker / Compose  | 24 / v2         | Контейнеризация        |
| Prometheus        | 2.51.0          | Сбор метрик            |
| Grafana           | 10.4.0          | Визуализация метрик    |
| Alertmanager      | 0.27.0          | Алертинг               |
| Mailpit           | latest          | SMTP-заглушка          |
| SpringDoc OpenAPI | 2.5.0           | Swagger UI             |
| Micrometer        | (BOM)           | Инструментирование JVM |
