package org.kuraterut.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
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

@RestController
@RequestMapping("/api/products/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CreateCategoryUseCase createCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;

    //TODO Прописать Postman

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryResponse createCategory(@RequestBody @Valid CreateCategoryRequest request) {
        return createCategoryUseCase.createCategory(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteCategoryById(@PathVariable("id") Long id) {
        deleteCategoryUseCase.deleteCategory(id);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteCategoryByName(@RequestParam("name") String name) {
        deleteCategoryUseCase.deleteCategory(name);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryResponse updateCategoryById(@PathVariable Long id,
                                                               @RequestBody @Valid UpdateCategoryRequest request) {
        return updateCategoryUseCase.updateCategoryById(id, request);
    }

    @PutMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryResponse updateCategoryByName(@RequestParam("name") String name,
                                                                 @RequestBody @Valid UpdateCategoryRequest request) {
        return updateCategoryUseCase.updateCategoryByName(name, request);
    }
    @GetMapping
    public Page<CategoryResponse> getAllCategories(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(defaultValue = "id") String sortBy,
                                                                   @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getCategoryUseCase.getAllCategories(pageable);
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Long id) {
        return getCategoryUseCase.getCategory(id);
    }

    @GetMapping("/name")
    public CategoryResponse getCategoryByName(@RequestParam("name") String name) {
        return getCategoryUseCase.getCategory(name);
    }
}
