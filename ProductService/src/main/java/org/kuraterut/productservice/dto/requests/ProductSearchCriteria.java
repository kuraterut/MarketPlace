package org.kuraterut.productservice.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSearchCriteria {
    private String name;
    private List<Long> categoryIds;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStockOnly = false;
    private String sortBy = "id";
    private String direction = "asc";
    private int page = 0;
    private int pageSize = 10;
}
