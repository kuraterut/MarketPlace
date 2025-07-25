package org.kuraterut.productservice.mapper;

import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.model.entity.Category;
import org.kuraterut.productservice.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {
    public Category toEntity(CreateCategoryRequest createCategoryRequest) {
        Category category = new Category();
        category.setName(createCategoryRequest.getName());
        category.setDescription(createCategoryRequest.getDescription());
        return category;
    }

    public CategoryResponse toResponse(Category category) {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        categoryResponse.setDescription(category.getDescription());
        categoryResponse.setCreatedAt(category.getCreatedAt());
        categoryResponse.setUpdatedAt(category.getUpdatedAt());
        Set<Long> productIds = category.getProducts().stream().map(Product::getId).collect(Collectors.toSet());
        categoryResponse.setProductIds(productIds);
        return categoryResponse;
    }

    public Page<CategoryResponse> toResponses(Page<Category> categories) {
        return categories.map(this::toResponse);
    }

    public void toEntity(Category category, UpdateCategoryRequest request) {
        category.setName(request.getName() == null ? category.getName() : request.getName());
        category.setDescription(request.getDescription() == null ? category.getDescription() : request.getDescription());
    }
}
