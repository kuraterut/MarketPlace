package org.kuraterut.productservice.usecases.product;


import org.kuraterut.productservice.dto.requests.CreateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductResponse;

public interface CreateProductUseCase {
    ProductResponse createProduct(CreateProductRequest product, Long userId);
}
