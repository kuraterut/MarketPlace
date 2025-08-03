package org.kuraterut.productservice.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryListResponse;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.exception.model.CategoryAlreadyExistsException;
import org.kuraterut.productservice.exception.model.CategoryNotFoundException;
import org.kuraterut.productservice.mapper.CategoryMapper;
import org.kuraterut.productservice.model.entity.Category;
import org.kuraterut.productservice.repository.CategoryRepository;
import org.kuraterut.productservice.repository.ProductRepository;
import org.kuraterut.productservice.usecases.category.CreateCategoryUseCase;
import org.kuraterut.productservice.usecases.category.DeleteCategoryUseCase;
import org.kuraterut.productservice.usecases.category.GetCategoryUseCase;
import org.kuraterut.productservice.usecases.category.UpdateCategoryUseCase;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "categories")
public class CategoryService implements CreateCategoryUseCase, DeleteCategoryUseCase, GetCategoryUseCase, UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        try{
            Category category = categoryMapper.toEntity(request);
            return categoryMapper.toResponse(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new CategoryAlreadyExistsException(e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(key = "#categoryId")
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found by id: " + categoryId);
        }

        productRepository.clearCategoryForProductsByCategoryId(categoryId);

        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional
    @CacheEvict(key = "#name")
    public void deleteCategory(String name) {

        if (!categoryRepository.existsByName(name)) {
            throw new CategoryNotFoundException("Category not found by name: " + name);
        }
        productRepository.clearCategoryForProductsByCategoryName(name);

        categoryRepository.deleteByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_categories_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public CategoryListResponse getAllCategories(Pageable pageable) {
        return categoryMapper.toResponses(categoryRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#name", unless = "#result == null")
    public CategoryResponse getCategory(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by name: " + name));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id", unless = "#result == null")
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by id: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "#name"),
            @CacheEvict(key = "#result.id")
    })
    public CategoryResponse updateCategoryByName(String name, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by name: " + name));
        categoryMapper.toEntity(category, request);
        return categoryMapper.toResponse(categoryRepository.saveAndFlush(category));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "#result.name")
    })
    public CategoryResponse updateCategoryById(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by id: " + id));
        if(!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())){
            throw new CategoryAlreadyExistsException("Category is already exists with name: " + request.getName());
        }
        categoryMapper.toEntity(category, request);
        return categoryMapper.toResponse(categoryRepository.saveAndFlush(category));
    }
}
