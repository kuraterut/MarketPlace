package org.kuraterut.productservice.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Creating product request")
public class CreateProductRequest {
    @NotBlank(message = "Name must contain at least one character")
    @Schema(description = "Product name", example = "Lamp", requiredMode = REQUIRED)
    private String name;

    @Schema(description = "Product description", example = "Lamp for flat", requiredMode = NOT_REQUIRED)
    private String description;

    @NotNull
    @Positive(message = "Price must be greater than 0")
    @Schema(description = "Product price", example = "100.0", requiredMode = REQUIRED)
    private BigDecimal price;

    @NotBlank(message = "Category must contain at least one character")
    @Schema(description = "Product category name", example = "House", requiredMode = REQUIRED)
    private String category;

    @Positive
    @NotNull(message = "Stock must be greater than 0")
    @Schema(description = "Product stock", example = "100", requiredMode = REQUIRED)
    private Long stock;
}
