package org.kuraterut.orderservice.model;


public enum OrderStatus {
    CREATED,
    PRODUCT_RESERVED,
    PRODUCT_RESERVATION_FAILED,
    PENDING_PAYMENT,
    PAYMENT_FAILED_NOT_FOUND,
    PAYMENT_FAILED_NOT_ENOUGH_MONEY,
    COMPLETED,
    CANCELLED
}
