package org.kuraterut.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.kuraterut.productservice.dto.requests.CreateProductRequest;
import org.kuraterut.productservice.dto.requests.UpdateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductResponse;
import org.kuraterut.productservice.usecases.product.CreateProductUseCase;
import org.kuraterut.productservice.usecases.product.DeleteProductUseCase;
import org.kuraterut.productservice.usecases.product.GetProductUseCase;
import org.kuraterut.productservice.usecases.product.UpdateProductUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final CreateProductUseCase createProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final GetProductUseCase getProductUseCase;
    @PostMapping
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid CreateProductRequest createProductRequest,
                                                         @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        log.info("(Product Service Controller) User ID: {}", userId);
        return ResponseEntity.ok(createProductUseCase.createProduct(createProductRequest, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id,
                                              @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        List<String> roles = authPrincipal.getRoles();
        deleteProductUseCase.deleteProduct(id, userId, roles);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") Long id,
                                                         @RequestBody @Valid UpdateProductRequest request,
                                                         @AuthenticationPrincipal AuthPrincipal authPrincipal){
        Long userId = authPrincipal.getUserId();
        return ResponseEntity.ok(updateProductUseCase.updateProduct(id, request, userId));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(defaultValue = "id") String sortBy,
                                                                @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(getProductUseCase.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(getProductUseCase.getProductByProductId(id));
    }

    @GetMapping("/filter/prefix")
    public ResponseEntity<Page<ProductResponse>> getProductsByPrefix(@RequestParam("prefix") String prefix,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestParam(defaultValue = "id") String sortBy,
                                                                     @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(getProductUseCase.getProductsStartingWithPrefix(prefix, pageable));
    }

    @GetMapping("/filter/seller")
    public ResponseEntity<Page<ProductResponse>> getProductsBySeller(@RequestParam("sellerId") Long sellerId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestParam(defaultValue = "id") String sortBy,
                                                                     @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(getProductUseCase.getProductsBySellerId(sellerId, pageable));
    }

    @GetMapping("/filter/category/{id}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(@PathVariable("id") Long id,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size,
                                                                       @RequestParam(defaultValue = "id") String sortBy,
                                                                       @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(getProductUseCase.getProductsByCategoryId(id, pageable));
    }

    @GetMapping("/filter/category/name")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategoryName(@RequestParam("categoryName") String categoryName,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @RequestParam(defaultValue = "id") String sortBy,
                                                                           @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(getProductUseCase.getProductsByCategoryName(categoryName, pageable));
    }

    @GetMapping("/filter/price")
    public ResponseEntity<Page<ProductResponse>> getProductsByPriceBetween(@RequestParam("min") BigDecimal min,
                                                                           @RequestParam("max") BigDecimal max,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @RequestParam(defaultValue = "id") String sortBy,
                                                                           @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(getProductUseCase.getProductsByPriceBetween(min, max, pageable));
    }
}
