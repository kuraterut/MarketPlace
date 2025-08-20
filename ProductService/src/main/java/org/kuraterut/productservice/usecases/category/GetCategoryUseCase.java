package org.kuraterut.productservice.usecases.category;

import org.kuraterut.productservice.dto.responses.CategoryListResponse;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.springframework.data.domain.Pageable;

public interface GetCategoryUseCase {
    CategoryListResponse getAllCategories(Pageable pageable);
    CategoryResponse getCategoryByName(String name);
    CategoryResponse getCategoryById(Long id);
}
