package org.kuraterut.productservice.usecases.category;

import org.kuraterut.productservice.dto.responses.CategoryListResponse;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetCategoryUseCase {
    CategoryListResponse getAllCategories(Pageable pageable);
    CategoryResponse getCategory(String name);
    CategoryResponse getCategory(Long id);
}
