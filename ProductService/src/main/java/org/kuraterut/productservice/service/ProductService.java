package org.kuraterut.productservice.service;

import lombok.RequiredArgsConstructor;
import org.kuraterut.productservice.dto.requests.CreateProductRequest;
import org.kuraterut.productservice.dto.requests.UpdateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductListResponse;
import org.kuraterut.productservice.dto.responses.ProductResponse;
import org.kuraterut.productservice.exception.model.CategoryNotFoundException;
import org.kuraterut.productservice.exception.model.PermissionDeniedException;
import org.kuraterut.productservice.exception.model.ProductNotFoundException;
import org.kuraterut.productservice.mapper.ProductMapper;
import org.kuraterut.productservice.model.entity.Category;
import org.kuraterut.productservice.model.entity.Product;
import org.kuraterut.productservice.repository.CategoryRepository;
import org.kuraterut.productservice.repository.ProductRepository;
import org.kuraterut.productservice.usecases.product.CreateProductUseCase;
import org.kuraterut.productservice.usecases.product.DeleteProductUseCase;
import org.kuraterut.productservice.usecases.product.GetProductUseCase;
import org.kuraterut.productservice.usecases.product.UpdateProductUseCase;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "products")
public class ProductService implements CreateProductUseCase, DeleteProductUseCase, GetProductUseCase, UpdateProductUseCase {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
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
    @CacheEvict(key = "#id")
    public void deleteProduct(Long id, Long userId) {
        boolean isOwner = productRepository.existsByIdAndSellerId(id, userId);

        if (!isOwner) {
            throw new PermissionDeniedException("Permission Denied. You are not allowed to delete this product");
        }

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found by id: " + id);
        }

        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(key = "#id")
    public void adminDeleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found by id: " + id);
        }
        productRepository.deleteById(id);
    }



    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_products_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getAllProducts(Pageable pageable) {
        return productMapper.toResponses(productRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#productId", unless = "#result == null")
    public ProductResponse getProductByProductId(Long productId) {
        return productMapper.toResponse(productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by id: " + productId)));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'prefix_products_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsStartingWithPrefix(String prefix, Pageable pageable) {
        return productMapper.toResponses(productRepository.findByNameStartingWithIgnoreCase(prefix, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'sellerId_products_' + #sellerId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsBySellerId(Long sellerId, Pageable pageable) {
        return productMapper.toResponses(productRepository.findBySellerId(sellerId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'categoryId_products_' + #categoryId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsByCategoryId(Long categoryId, Pageable pageable) {
        return productMapper.toResponses(productRepository.findByCategoryId(categoryId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'categoryName_products_' + #categoryName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsByCategoryName(String categoryName, Pageable pageable) {
        return productMapper.toResponses(productRepository.findByCategoryName(categoryName, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'price_between_products_' + #min + '_' + #max + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable) {
        return productMapper.toResponses(productRepository.findByPriceBetween(min, max, pageable));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
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
