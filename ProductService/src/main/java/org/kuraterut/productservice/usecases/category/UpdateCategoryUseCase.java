package org.kuraterut.productservice.usecases.category;


import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryResponse;

public interface UpdateCategoryUseCase {
    CategoryResponse updateCategoryByName(String name, UpdateCategoryRequest request);
    CategoryResponse updateCategoryById(Long id, UpdateCategoryRequest request);
}
