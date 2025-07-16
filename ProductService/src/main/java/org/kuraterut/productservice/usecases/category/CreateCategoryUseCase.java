package org.kuraterut.productservice.usecases.category;

import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.model.Category;

public interface CreateCategoryUseCase {
    CategoryResponse createCategory(CreateCategoryRequest request);
}
