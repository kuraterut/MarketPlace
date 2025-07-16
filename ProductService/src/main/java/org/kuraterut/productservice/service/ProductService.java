package org.kuraterut.productservice.service;

import lombok.RequiredArgsConstructor;
import org.kuraterut.productservice.dto.requests.CreateProductRequest;
import org.kuraterut.productservice.dto.requests.UpdateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductResponse;
import org.kuraterut.productservice.exception.model.CategoryNotFoundException;
import org.kuraterut.productservice.exception.model.PermissionDeniedException;
import org.kuraterut.productservice.exception.model.ProductNotFoundException;
import org.kuraterut.productservice.mapper.ProductMapper;
import org.kuraterut.productservice.model.Category;
import org.kuraterut.productservice.model.Product;
import org.kuraterut.productservice.repository.CategoryRepository;
import org.kuraterut.productservice.repository.ProductRepository;
import org.kuraterut.productservice.usecases.product.CreateProductUseCase;
import org.kuraterut.productservice.usecases.product.DeleteProductUseCase;
import org.kuraterut.productservice.usecases.product.GetProductUseCase;
import org.kuraterut.productservice.usecases.product.UpdateProductUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService implements CreateProductUseCase, DeleteProductUseCase, GetProductUseCase, UpdateProductUseCase {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, Long userId) {
        Product product = productMapper.toEntity(request);
        Category category = categoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by name: " + request.getCategory()));
        product.setCategory(category);
        product.setSellerId(userId);
        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, Long userId, List<String> roles) {
        boolean isOwner = productRepository.existsByIdAndSellerId(id, userId);

        if (!isOwner && !roles.contains("ADMIN")) {
            throw new PermissionDeniedException("Permission Denied. You are not allowed to delete this product");
        }

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found by id: " + id);
        }

        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productMapper.toResponses(productRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByProductId(Long productId) {
        return productMapper.toResponse(productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by id: " + productId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsStartingWithPrefix(String prefix, Pageable pageable) {
        return productMapper.toResponses(productRepository.findByNameStartingWithIgnoreCase(prefix, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsBySellerId(Long sellerId, Pageable pageable) {
        return productMapper.toResponses(productRepository.findBySellerId(sellerId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        return productMapper.toResponses(productRepository.findByCategoryId(categoryId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategoryName(String categoryName, Pageable pageable) {
        return productMapper.toResponses(productRepository.findByCategoryName(categoryName, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable) {
        return productMapper.toResponses(productRepository.findByPriceBetween(min, max, pageable));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request, Long userId){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by id: " + id));
        if(!Objects.equals(product.getSellerId(), userId)){
            throw new PermissionDeniedException("Permission Denied. You are not allowed to update this product");
        }
        productMapper.toEntity(product, request);
        if(request.getCategory() != null){
            Category category = categoryRepository.findByName(request.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found by name: " + request.getCategory()));
            product.setCategory(category);
        }
        return productMapper.toResponse(productRepository.save(product));
    }


}
