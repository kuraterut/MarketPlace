package org.kuraterut.productservice.usecases.product;

import org.apache.kafka.common.protocol.types.Field;

import java.util.List;

public interface DeleteProductUseCase {
    void deleteProduct(Long id, Long userId, List<String> roles);
}
