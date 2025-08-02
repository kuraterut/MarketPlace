package org.kuraterut.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.kuraterut.productservice.dto.requests.CreateProductRequest;
import org.kuraterut.productservice.dto.requests.UpdateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductListResponse;
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
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Product Controller", description = "Controller for product manipulations")
public class ProductController {
    private final CreateProductUseCase createProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final GetProductUseCase getProductUseCase;


    @PostMapping
    @PreAuthorize("hasAuthority('SELLER')")
    @Operation(summary = "Create Product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductResponse createProduct(
            @Parameter(description = "Create product request") @RequestBody @Valid CreateProductRequest createProductRequest,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return createProductUseCase.createProduct(createProductRequest, userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    @Operation(summary = "Delete Product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public void deleteProduct(
            @Parameter(description = "Product ID") @PathVariable("id") Long id,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        deleteProductUseCase.deleteProduct(id, userId);
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete Product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public void adminDeleteProduct(
            @Parameter(description = "Product ID") @PathVariable("id") Long id) {
        deleteProductUseCase.adminDeleteProduct(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    @Operation(summary = "Update Product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductResponse updateProduct(
            @Parameter(description = "Product ID") @PathVariable("id") Long id,
            @Parameter(description = "Update product request") @RequestBody @Valid UpdateProductRequest request,
            @AuthenticationPrincipal AuthPrincipal authPrincipal){
        Long userId = authPrincipal.getUserId();
        return updateProductUseCase.updateProduct(id, request, userId);
    }

    @GetMapping
    @Operation(summary = "Getting all Products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductListResponse getAllProducts(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getProductUseCase.getAllProducts(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Getting Product by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductResponse getProductById(
            @Parameter(description = "Product ID") @PathVariable("id") Long id) {
        return getProductUseCase.getProductByProductId(id);
    }

    @GetMapping("/filter/prefix")
    @Operation(summary = "Getting all Products filter by prefix")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductListResponse getProductsByPrefix(
            @Parameter(description = "Product name prefix") @RequestParam("prefix") String prefix,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getProductUseCase.getProductsStartingWithPrefix(prefix, pageable);
    }

    @GetMapping("/filter/seller")
    @Operation(summary = "Getting all Products filter by seller ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductListResponse getProductsBySeller(
            @Parameter(description = "Seller ID") @RequestParam("sellerId") Long sellerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getProductUseCase.getProductsBySellerId(sellerId, pageable);
    }

    @GetMapping("/filter/category/{id}")
    @Operation(summary = "Getting all Products filter by category ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductListResponse getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable("id") Long id,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getProductUseCase.getProductsByCategoryId(id, pageable);
    }

    @GetMapping("/filter/category/name")
    @Operation(summary = "Getting all Products filter by category name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductListResponse getProductsByCategoryName(
            @Parameter(description = "Category name") @RequestParam("categoryName") String categoryName,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getProductUseCase.getProductsByCategoryName(categoryName, pageable);
    }

    @GetMapping("/filter/price")
    @Operation(summary = "Getting all Products filter by price between")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ProductListResponse getProductsByPriceBetween(
            @Parameter(description = "Min value of price") @RequestParam("min") BigDecimal min,
            @Parameter(description = "Max value of price") @RequestParam("max") BigDecimal max,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getProductUseCase.getProductsByPriceBetween(min, max, pageable);
    }
}
