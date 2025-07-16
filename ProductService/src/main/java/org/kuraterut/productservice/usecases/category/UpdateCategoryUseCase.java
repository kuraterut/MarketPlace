package org.kuraterut.productservice.usecases.category;


import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.model.Category;

public interface UpdateCategoryUseCase {
    CategoryResponse updateCategoryByName(String name, UpdateCategoryRequest request);
    CategoryResponse updateCategoryById(Long id, UpdateCategoryRequest request);
}
