package org.kuraterut.analyticsservice.repository


import org.kuraterut.analyticsservice.model.OrderAnalyticsRecord
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class OrderAnalyticsRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun save(record: OrderAnalyticsRecord) {
        jdbcTemplate.update(
            """
            INSERT INTO orders_analytics 
            (order_id, user_id, product_id, quantity, status, event_time)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            record.orderId,
            record.userId,
            record.productId,
            record.quantity,
            record.status.name,
            record.eventTime
        )
    }

    fun countOrdersByStatus(): Map<String, Long> {
        val sql = """
            SELECT status, count() as cnt
            FROM orders_analytics
            GROUP BY status
        """.trimIndent()

        return jdbcTemplate.query(sql) { rs, _ ->
            rs.getString("status") to rs.getLong("cnt")
        }.toMap()
    }
}
