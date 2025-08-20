package org.kuraterut.productservice.mapper;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class CategoryMapper {
    public Category toEntity(CreateCategoryRequest createCategoryRequest) {
        Category category = new Category();
        category.setName(createCategoryRequest.getName());
        category.setDescription(createCategoryRequest.getDescription());
        return category;
    }

    public CategoryResponse toResponse(Category category) {
        log.info("[CategoryMapper:toResponse] Category: {}", category);
        CategoryResponse categoryResponse = new CategoryResponse();
        log.info("[CategoryMapper:toResponse] 1");
        categoryResponse.setId(category.getId());
        log.info("[CategoryMapper:toResponse] 2");
        categoryResponse.setName(category.getName());
        log.info("[CategoryMapper:toResponse] 3");
        categoryResponse.setDescription(category.getDescription());
        log.info("[CategoryMapper:toResponse] 4");
        String createdAt = category.getCreatedAt()==null?null:category.getCreatedAt().toString();
        log.info("[CategoryMapper:toResponse] 5");
        String updatedAt = category.getUpdatedAt()==null?null:category.getUpdatedAt().toString();
        log.info("[CategoryMapper:toResponse] 6");
        categoryResponse.setCreatedAt(createdAt);
        log.info("[CategoryMapper:toResponse] 7");
        categoryResponse.setUpdatedAt(updatedAt);
        log.info("[CategoryMapper:toResponse] 8");
        Set<Long> productIds = category.getProducts().stream().map(Product::getId).collect(Collectors.toSet());
        log.info("[CategoryMapper:toResponse] 9");
        categoryResponse.setProductIds(productIds);
        log.info("[CategoryMapper:toResponse] 10");
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
