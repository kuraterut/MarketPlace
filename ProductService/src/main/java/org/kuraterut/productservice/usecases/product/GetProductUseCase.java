package org.kuraterut.productservice.usecases.product;

import org.kuraterut.productservice.dto.responses.ProductListResponse;
import org.kuraterut.productservice.dto.responses.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface GetProductUseCase {
    ProductListResponse getAllProducts(Pageable pageable);
    ProductResponse getProductByProductId(Long productId);
    ProductListResponse getProductsStartingWithPrefix(String prefix, Pageable pageable);
    ProductListResponse getProductsBySellerId(Long sellerId, Pageable pageable);
    ProductListResponse getProductsByCategoryId(Long categoryId, Pageable pageable);
    ProductListResponse getProductsByCategoryName(String categoryName, Pageable pageable);
    ProductListResponse getProductsByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);
}
