package org.kuraterut.analyticsservice.model

import java.time.LocalDateTime

data class OrderAnalyticsRecord(
    val orderId: Long,
    val userId: Long,
    val productId: Long,
    val quantity: Long,
    val status: OrderStatus,
    val eventTime: LocalDateTime = LocalDateTime.now()
)
