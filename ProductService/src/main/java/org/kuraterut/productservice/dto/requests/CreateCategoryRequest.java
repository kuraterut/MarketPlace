package org.kuraterut.productservice.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Creating category request")
public class CreateCategoryRequest {
    @NotBlank(message = "Name must contain at least one character")
    @Schema(description = "Category name", example = "House", requiredMode = REQUIRED)
    private String name;

    @Schema(description = "Category description", example = "House products", requiredMode = NOT_REQUIRED)
    private String description;
}
