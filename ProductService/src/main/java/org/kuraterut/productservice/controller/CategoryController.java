package org.kuraterut.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryListResponse;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.usecases.category.CreateCategoryUseCase;
import org.kuraterut.productservice.usecases.category.DeleteCategoryUseCase;
import org.kuraterut.productservice.usecases.category.GetCategoryUseCase;
import org.kuraterut.productservice.usecases.category.UpdateCategoryUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/products/category")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Category controller", description = "Controller for categories manipulations")
public class CategoryController {
    private final CreateCategoryUseCase createCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;


    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Category is already exists"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public CategoryResponse createCategory(
            @Parameter(description = "Create category request") @RequestBody @Valid CreateCategoryRequest request) {
        return createCategoryUseCase.createCategory(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete Category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public void deleteCategoryById(
            @Parameter(description = "Category ID") @PathVariable("id") Long id) {
        deleteCategoryUseCase.deleteCategory(id);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete Category by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public void deleteCategoryByName(
            @Parameter(description = "Category Name") @RequestParam("name") String name) {
        deleteCategoryUseCase.deleteCategory(name);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update Category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category is already exists"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public CategoryResponse updateCategoryById(
            @Parameter(description = "Category ID") @PathVariable("id") Long id,
            @Parameter(description = "Update Category request") @RequestBody @Valid UpdateCategoryRequest request) {
        return updateCategoryUseCase.updateCategoryById(id, request);
    }

    @PutMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update Category by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public CategoryResponse updateCategoryByName(
            @Parameter(description = "Category name") @RequestParam("name") String name,
            @Parameter(description = "Update Category request") @RequestBody @Valid UpdateCategoryRequest request) {
        return updateCategoryUseCase.updateCategoryByName(name, request);
    }


    @GetMapping
    @Operation(summary = "Getting all Categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public CategoryListResponse getAllCategories(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getCategoryUseCase.getAllCategories(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Getting Category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public CategoryResponse getCategoryById(
            @Parameter(description = "Category ID") @PathVariable("id") Long id) {
        return getCategoryUseCase.getCategory(id);
    }

    @GetMapping("/name")
    @Operation(summary = "Getting Category by Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public CategoryResponse getCategoryByName(
            @Parameter(description = "Category name") @RequestParam("name") String name) {
        return getCategoryUseCase.getCategory(name);
    }
}
