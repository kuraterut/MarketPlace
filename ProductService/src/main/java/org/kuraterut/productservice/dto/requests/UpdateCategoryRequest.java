package org.kuraterut.productservice.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Update category request")
public class UpdateCategoryRequest {
    @Schema(description = "Category name", example = "House")
    private String name;
    @Schema(description = "Category description", example = "House products")
    private String description;
}
