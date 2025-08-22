package org.kuraterut.analyticsservice.service

import org.kuraterut.analyticsservice.model.OrderAnalyticsRecord
import org.kuraterut.analyticsservice.model.OrderCreatedEvent
import org.kuraterut.analyticsservice.model.OrderStatus
import org.kuraterut.analyticsservice.repository.OrderAnalyticsRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderAnalyticsService(
    private val repository: OrderAnalyticsRepository
) {
    fun saveOrderEvent(event: OrderCreatedEvent) {
        event.items.forEach { item ->
            val record = OrderAnalyticsRecord(
                orderId = event.orderId,
                userId = event.userId,
                productId = item.productId,
                quantity = item.quantity,
                status = OrderStatus.CREATED,
                eventTime = LocalDateTime.now()
            )
            repository.save(record)
        }
    }

    fun getOrdersStatsByStatus(): Map<String, Long> {
        return repository.countOrdersByStatus()
    }
}
