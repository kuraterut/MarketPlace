package org.kuraterut.analyticsservice.controller

import org.kuraterut.analyticsservice.service.OrderAnalyticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val orderAnalyticsService: OrderAnalyticsService
) {

    /**
     * Количество заказов по каждому статусу
     *
     * Пример ответа:
     * {
     *   "CREATED": 5,
     *   "SUCCESS": 12,
     *   "FAILED_NO_FUNDS": 3
     * }
     */
    @GetMapping("/stats")
    fun getOrdersStats(): Map<String, Long> {
        return orderAnalyticsService.getOrdersStatsByStatus()
    }
}
