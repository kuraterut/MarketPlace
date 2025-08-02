package org.kuraterut.productservice.mapper;

import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryListResponse;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.model.entity.Category;
import org.kuraterut.productservice.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
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
        String createdAt = category.getCreatedAt()==null?null:category.getCreatedAt().toString();
        String updatedAt = category.getUpdatedAt()==null?null:category.getUpdatedAt().toString();
        categoryResponse.setCreatedAt(createdAt);
        categoryResponse.setUpdatedAt(updatedAt);
        Set<Long> productIds = category.getProducts().stream().map(Product::getId).collect(Collectors.toSet());
        categoryResponse.setProductIds(productIds);
        return categoryResponse;
    }

    public CategoryListResponse toResponses(Page<Category> categories) {
        return new CategoryListResponse(categories.map(this::toResponse).stream().toList());
    }

    public void toEntity(Category category, UpdateCategoryRequest request) {
        category.setName(request.getName() == null ? category.getName() : request.getName());
        category.setDescription(request.getDescription() == null ? category.getDescription() : request.getDescription());
    }
}
