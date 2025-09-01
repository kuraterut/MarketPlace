package org.kuraterut.analyticsservice.consumer


import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.kuraterut.analyticsservice.model.OrderCreatedEvent
import org.kuraterut.analyticsservice.service.OrderAnalyticsService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class OrderEventsConsumer(
    private val orderAnalyticsService: OrderAnalyticsService,
    private val objectMapper: ObjectMapper
) {
    @KafkaListener(
        topics = ["order-created"],
        groupId = "analytics-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consume(message: String) {
        try {
            val event = objectMapper.readValue(message, OrderCreatedEvent::class.java)
            log.info {"✅ Десериализовано событие: $event"}

            orderAnalyticsService.saveOrderEvent(event)
            log.info { "Сообщение сохранено в ClickHouse" }
        } catch (ex: JsonProcessingException) {
            log.error {"❌ Ошибка десериализации сообщения: $message, ${ex.message}"}
        }
    }
}
