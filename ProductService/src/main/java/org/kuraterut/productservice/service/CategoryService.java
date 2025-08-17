package org.kuraterut.productservice.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "categories")
@Slf4j
public class CategoryService implements CreateCategoryUseCase, DeleteCategoryUseCase, GetCategoryUseCase, UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("[CategoryService:createCategory] Start createCategory");
        try{
            Category category = categoryMapper.toEntity(request);
            category = categoryRepository.saveAndFlush(category);
            log.info("[CategoryService:createCategory] Category created and saved");
            return categoryMapper.toResponse(category);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            log.warn("[CategoryService:createCategory] Category already exists: {}", e.getMessage());
            throw new CategoryAlreadyExistsException(e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(key = "#categoryId")
    public void deleteCategoryById(Long categoryId) {
        log.info("[CategoryService:deleteCategoryById] Start deleteCategoryById");
        if (!categoryRepository.existsById(categoryId)) {
            log.warn("[CategoryService:deleteCategoryById] Category does not exist by categoryId: {}", categoryId);
            throw new CategoryNotFoundException("Category not found by id: " + categoryId);
        }

        productRepository.clearCategoryForProductsByCategoryId(categoryId);
        log.info("[CategoryService:deleteCategoryById] clear category for products by categoryId: {}", categoryId);
        categoryRepository.deleteById(categoryId);
        log.info("[CategoryService:deleteCategoryById] Category deleted");
    }

    @Override
    @Transactional
    @CacheEvict(key = "#name")
    public void deleteCategoryByName(String name) {
        log.info("[CategoryService:deleteCategoryByName] Start deleteCategoryByName");
        if (!categoryRepository.existsByName(name)) {
            log.warn("[CategoryService:deleteCategoryByName] Category does not exist by name: {}", name);
            throw new CategoryNotFoundException("Category not found by name: " + name);
        }
        productRepository.clearCategoryForProductsByCategoryName(name);
        log.info("[CategoryService:deleteCategoryByName] clear category for products by name: {}", name);
        categoryRepository.deleteByName(name);
        log.info("[CategoryService:deleteCategoryByName] Category deleted By Name");
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_categories_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public CategoryListResponse getAllCategories(Pageable pageable) {
        log.info("[CategoryService:getAllCategories] Start getAllCategories");
        Page<Category> categories = categoryRepository.findAll(pageable);
        log.info("[CategoryService:getAllCategories] categories found");
        return categoryMapper.toResponses(categories);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#name", unless = "#result == null")
    public CategoryResponse getCategoryByName(String name) {
        log.info("[CategoryService:getCategoryByName] Start getCategoryByName");
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("[CategoryService:getCategoryByName] Category not found by name: {}", name);
                    return new CategoryNotFoundException("Category not found by name: " + name);
                });
        log.info("[CategoryService:getCategoryByName] Category found");
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id", unless = "#result == null")
    public CategoryResponse getCategoryById(Long id) {
        log.info("[CategoryService:getCategoryById] Start getCategoryById");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[CategoryService:getCategoryById] Category not found by id: {}", id);
                    return new CategoryNotFoundException("Category not found by id: " + id);
                });
        log.info("[CategoryService:getCategoryById] Category found");
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public CategoryResponse updateCategoryByName(String name, UpdateCategoryRequest request) {
        log.info("[CategoryService:updateCategoryByName] Start updateCategoryByName");
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("[CategoryService:updateCategoryByName] Category not found by name: {}", name);
                    return new CategoryNotFoundException("Category not found by name: " + name);
                });
        if(!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())){
            log.warn("[CategoryService:updateCategoryByName] Category already exists with name: {}", request.getName());
            throw new CategoryAlreadyExistsException("Category is already exists with name: " + request.getName());
        }
        categoryMapper.toEntity(category, request);
        category = categoryRepository.saveAndFlush(category);
        log.info("[CategoryService:updateCategoryByName] Category updated");
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public CategoryResponse updateCategoryById(Long id, UpdateCategoryRequest request) {
        log.info("[CategoryService:updateCategoryById] Start updateCategoryById");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[CategoryService:updateCategoryById] Category not found by id: {}", id);
                    return new CategoryNotFoundException("Category not found by id: " + id);
                });

        log.info("[CategoryService:updateCategoryById] Category found");
        if(!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())){
            log.warn("[CategoryService:updateCategoryById] Category already exists with name: {}", request.getName());
            throw new CategoryAlreadyExistsException("Category is already exists with name: " + request.getName());
        }

        categoryMapper.toEntity(category, request);
        category = categoryRepository.saveAndFlush(category);
        log.info("[CategoryService:updateCategoryById] Category updated");

        return categoryMapper.toResponse(category);
    }
}
