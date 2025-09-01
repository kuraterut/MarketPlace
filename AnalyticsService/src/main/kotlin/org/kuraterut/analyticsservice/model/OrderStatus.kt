package org.kuraterut.analyticsservice.model


enum class OrderStatus {
    CREATED,
    SUCCESS,
    FAILED_OUT_OF_STOCK,
    FAILED_NO_PRODUCT,
    FAILED_NO_FUNDS,
    FAILED_NO_ACCOUNT,
    CANCELLED
}
