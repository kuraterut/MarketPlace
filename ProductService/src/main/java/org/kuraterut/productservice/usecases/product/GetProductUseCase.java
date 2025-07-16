package org.kuraterut.productservice.usecases.product;

import org.kuraterut.productservice.dto.responses.ProductResponse;
import org.kuraterut.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface GetProductUseCase {
    Page<ProductResponse> getAllProducts(Pageable pageable);
    ProductResponse getProductByProductId(Long productId);
    Page<ProductResponse> getProductsStartingWithPrefix(String prefix, Pageable pageable);
    Page<ProductResponse> getProductsBySellerId(Long sellerId, Pageable pageable);
    Page<ProductResponse> getProductsByCategoryId(Long categoryId, Pageable pageable);
    Page<ProductResponse> getProductsByCategoryName(String categoryName, Pageable pageable);
    Page<ProductResponse> getProductsByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);
}
