package org.kuraterut.analyticsservice.model

data class OrderCreatedEvent(
    val orderId: Long,
    val userId: Long,
    val items: List<OrderItemDto>
)
