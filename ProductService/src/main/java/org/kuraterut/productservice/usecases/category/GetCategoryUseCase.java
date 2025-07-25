package org.kuraterut.productservice.usecases.category;

import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetCategoryUseCase {
    Page<CategoryResponse> getAllCategories(Pageable pageable);
    CategoryResponse getCategory(String name);
    CategoryResponse getCategory(Long id);
}
