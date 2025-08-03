package org.kuraterut.productservice.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Update product request")
public class UpdateProductRequest {
    @NotBlank(message = "Product name must be not empty")
    @Schema(description = "Product name", example = "Lamp")
    private String name;

    @Schema(description = "Product description", example = "Lamp for house")
    private String description;

    @Positive(message = "Price must be greater than 0")
    @NotNull(message = "Product price must be not null")
    @Schema(description = "Product price", example = "100.0")
    private BigDecimal price;

    @NotBlank(message = "Category name must be not empty")
    @Schema(description = "Product category name", example = "House")
    private String category;

    @Positive(message = "Stock must be greater than 0")
    @NotNull(message = "Product stock must be not null")
    @Schema(description = "Product stock", example = "100")
    private Long stock;
}
