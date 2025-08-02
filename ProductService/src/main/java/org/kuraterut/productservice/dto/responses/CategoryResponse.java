package org.kuraterut.productservice.dto.responses;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response with Category Info")
public class CategoryResponse {
    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category Name", example = "House")
    private String name;

    @Schema(description = "Category Description", example = "House products")
    private String description;

    @Schema(description = "Creating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String createdAt;

    @Schema(description = "Updating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String updatedAt;

    @Schema(description = "Set of Product IDs", example = "{1, 2}")
    private Set<Long> productIds;
}
