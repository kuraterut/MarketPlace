package org.kuraterut.productservice.usecases.product;

import org.kuraterut.productservice.dto.requests.UpdateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductResponse;

public interface UpdateProductUseCase {
    ProductResponse updateProduct(Long id, UpdateProductRequest request, Long userId);
}
