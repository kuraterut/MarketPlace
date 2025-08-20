package org.kuraterut.productservice.usecases.product;


public interface DeleteProductUseCase {
    void deleteProduct(Long id, Long userId);
    void adminDeleteProduct(Long id);
}
