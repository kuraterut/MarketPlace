package org.kuraterut.productservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ProductService implements CreateProductUseCase, DeleteProductUseCase, GetProductUseCase, UpdateProductUseCase {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request, Long userId) {
        log.info("[ProductService:createProduct] Start method createProduct, request: {}, userId: {}", request, userId);
        Product product = productMapper.toEntity(request);
        Category category = categoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> {
                    log.warn("[ProductService:createProduct] Category not found: {}", request.getCategory());
                    return new CategoryNotFoundException("Category not found by name: " + request.getCategory());
                });
        log.info("[ProductService:createProduct] Category found: {}", category);
        product.setCategory(category);
        product.setSellerId(userId);
        product = productRepository.saveAndFlush(product);
        log.info("[ProductService:createProduct] Product created and saved: {}", product);
        log.info("[ProductService:createProduct] End method createProduct");
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    public void deleteProduct(Long productId, Long userId) {
        log.info("[ProductService:deleteProduct] Start method deleteProduct, productId: {}, userId: {}", productId, userId);
        if (!productRepository.existsById(productId)) {
            log.warn("[ProductService:deleteProduct] Product not found, productId: {}", productId);
            throw new ProductNotFoundException("Product not found by productId: " + productId);
        }
        log.info("[ProductService:deleteProduct] Product exists");

        boolean isOwner = productRepository.existsByIdAndSellerId(productId, userId);
        if (!isOwner) {
            log.warn("[ProductService:deleteProduct] User is not owner of product, productId: {}, userId: {}", productId, userId);
            throw new PermissionDeniedException("Permission Denied. You are not allowed to delete this product");
        }
        productRepository.deleteById(productId);
        log.info("[ProductService:deleteProduct] Product deleted");
    }

    @Override
    @Transactional
    @CacheEvict(key = "#productId")
    public void adminDeleteProduct(Long productId) {
        log.info("[ProductService:adminDeleteProduct] Start method adminDeleteProduct, productId: {}", productId);
        if (!productRepository.existsById(productId)) {
            log.warn("[ProductService:adminDeleteProduct] Product not found, productId: {}", productId);
            throw new ProductNotFoundException("Product not found by productId: " + productId);
        }
        productRepository.deleteById(productId);
        log.info("[ProductService:adminDeleteProduct] Product deleted");
    }



    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_products_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getAllProducts(Pageable pageable) {
        log.info("[ProductService:getAllProducts] Start method getAllProducts");
        Page<Product> products = productRepository.findAll(pageable);
        log.info("[ProductService:getAllProducts] Products found");
        return productMapper.toResponses(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#productId", unless = "#result == null")
    public ProductResponse getProductByProductId(Long productId) {
        log.info("[ProductService:getProductByProductId] Start method getProductByProductId");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("[ProductService:getProductByProductId] Product not found, productId: {}", productId);
                    return new ProductNotFoundException("Product not found by id: " + productId);
                });
        log.info("[ProductService:getProductByProductId] Product found");
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'prefix_products_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsStartingWithPrefix(String prefix, Pageable pageable) {
        log.info("[ProductService:getProductsStartingWithPrefix] Start method getProductsStartingWithPrefix");
        Page<Product> products = productRepository.findByNameStartingWithIgnoreCase(prefix, pageable);
        log.info("[ProductService:getProductsStartingWithPrefix] Products found");
        return productMapper.toResponses(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'sellerId_products_' + #sellerId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsBySellerId(Long sellerId, Pageable pageable) {
        log.info("[ProductService:getProductsBySellerId] Start method getProductsBySellerId");
        Page<Product> products = productRepository.findBySellerId(sellerId, pageable);
        log.info("[ProductService:getProductsBySellerId] Products found");
        return productMapper.toResponses(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'categoryId_products_' + #categoryId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsByCategoryId(Long categoryId, Pageable pageable) {
        log.info("[ProductService:getProductsByCategoryId] Start method getProductsByCategoryId");
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        log.info("[ProductService:getProductsByCategoryId] Products found");
        return productMapper.toResponses(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'categoryName_products_' + #categoryName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsByCategoryName(String categoryName, Pageable pageable) {
        log.info("[ProductService:getProductsByCategoryName] Start method getProductsByCategoryName");
        Page<Product> products = productRepository.findByCategoryName(categoryName, pageable);
        log.info("[ProductService:getProductsByCategoryName] Products found");
        return productMapper.toResponses(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'price_between_products_' + #min + '_' + #max + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public ProductListResponse getProductsByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable) {
        log.info("[ProductService:getProductsByPriceBetween] Start method getProductsByPriceBetween");
        Page<Product> products = productRepository.findByPriceBetween(min, max, pageable);
        log.info("[ProductService:getProductsByPriceBetween] Products found");
        return productMapper.toResponses(products);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request, Long userId){
        log.info("[ProductService:updateProduct] Start method updateProduct");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("[ProductService:updateProduct] Product not found, productId: {}", productId);
                    return new ProductNotFoundException("Product not found by productId: " + productId);
                });
        log.info("[ProductService:updateProduct] Product found");
        if(!Objects.equals(product.getSellerId(), userId)){
            log.warn("[ProductService:updateProduct] Permission Denied, userId: {}", userId);
            throw new PermissionDeniedException("Permission Denied. You are not allowed to update this product");
        }

        productMapper.toEntity(product, request);
        if(request.getCategory() != null){
            Category category = categoryRepository.findByName(request.getCategory())
                    .orElseThrow(() -> {
                        log.warn("[ProductService:updateProduct] Category not found, category: {}", request.getCategory());
                        return new CategoryNotFoundException("Category not found by name: " + request.getCategory());
                    });
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.saveAndFlush(product);
        log.info("[ProductService:updateProduct] Product updated");
        return productMapper.toResponse(updatedProduct);
    }
}
