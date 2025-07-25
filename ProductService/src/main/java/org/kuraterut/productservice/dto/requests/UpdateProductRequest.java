package org.kuraterut.productservice.dto.requests;

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
public class UpdateProductRequest {
    @NotBlank(message = "Product name must be not empty")
    private String name;

    private String description;

    @Positive(message = "Price must be greater than 0")
    @NotNull(message = "Product price must be not null")
    private BigDecimal price;

    @NotBlank(message = "Category name must be not empty")
    private String category;

    @Positive(message = "Stock must be greater than 0")
    @NotNull(message = "Product stock must be not null")
    private Long stock;
}
