-- Создание базы, если не существует
CREATE DATABASE IF NOT EXISTS analytics;

-- Создание таблицы для заказов
CREATE TABLE IF NOT EXISTS analytics.orders_analytics
(
    order_id   UInt64,
    user_id    UInt64,
    product_id UInt64,
    quantity   UInt64,
    status     String,
    event_time DateTime64(9, 'UTC') DEFAULT now()
    )
    ENGINE = MergeTree
    ORDER BY (order_id, product_id);
