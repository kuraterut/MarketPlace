package org.kuraterut.productservice.dto.requests;

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
public class CreateProductRequest {
    @NotBlank(message = "Name must contain at least one character")
    private String name;

    private String description;

    @NotNull
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @NotBlank(message = "Category must contain at least one character")
    private String category;

    @Positive
    @NotNull(message = "Stock must be greater than 0")
    private Long stock;
}
