package org.kuraterut.productservice.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response with Product Info")
public class ProductResponse {
    @Schema(description = "Product ID", example = "1")
    private Long id;
    @Schema(description = "Product Name", example = "Lamp")
    private String name;
    @Schema(description = "Product Description", example = "Lamp for flat")
    private String description;
    @Schema(description = "Product unit price", example = "100.0")
    private BigDecimal price;
    @Schema(description = "Product category name", example = "House")
    private String category;
    @Schema(description = "Product inventory stock", example = "50")
    private Long stock;
    @Schema(description = "Product Seller ID", example = "1")
    private Long sellerId;
    @Schema(description = "Creating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String createdAt;
    @Schema(description = "Updating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String updatedAt;
}
