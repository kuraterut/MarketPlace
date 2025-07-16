package org.kuraterut.productservice.usecases.product;

import org.kuraterut.productservice.dto.requests.UpdateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductResponse;
import org.kuraterut.productservice.model.Product;

import java.math.BigDecimal;

public interface UpdateProductUseCase {
    ProductResponse updateProduct(Long id, UpdateProductRequest request, Long userId);
}
