package org.kuraterut.productservice.mapper;

import org.kuraterut.productservice.dto.requests.CreateProductRequest;
import org.kuraterut.productservice.dto.requests.UpdateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductResponse;
import org.kuraterut.productservice.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product toEntity(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        return product;
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(product.getId());
        productResponse.setName(product.getName());
        productResponse.setDescription(product.getDescription());
        productResponse.setPrice(product.getPrice());
        productResponse.setCategory(product.getCategory() == null ? null : product.getCategory().getName());
        productResponse.setStock(product.getStock());
        productResponse.setSellerId(product.getSellerId());
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }

    public Page<ProductResponse> toResponses(Page<Product> products) {
        return products.map(this::toResponse);
    }

    public void toEntity(Product product, UpdateProductRequest request) {
        product.setName(request.getName() == null ? product.getName() : request.getName());
        product.setDescription(request.getDescription() == null ? product.getDescription() : request.getDescription());
        product.setPrice(request.getPrice() == null ? product.getPrice() : request.getPrice());
        product.setStock(request.getStock() == null ? product.getStock() : request.getStock());
    }
}
