package org.kuraterut.productservice.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.exception.model.CategoryAlreadyExists;
import org.kuraterut.productservice.exception.model.CategoryNotFoundException;
import org.kuraterut.productservice.exception.model.ProductNotFoundException;
import org.kuraterut.productservice.mapper.CategoryMapper;
import org.kuraterut.productservice.model.Category;
import org.kuraterut.productservice.model.Product;
import org.kuraterut.productservice.repository.CategoryRepository;
import org.kuraterut.productservice.repository.ProductRepository;
import org.kuraterut.productservice.usecases.category.CreateCategoryUseCase;
import org.kuraterut.productservice.usecases.category.DeleteCategoryUseCase;
import org.kuraterut.productservice.usecases.category.GetCategoryUseCase;
import org.kuraterut.productservice.usecases.category.UpdateCategoryUseCase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements CreateCategoryUseCase, DeleteCategoryUseCase, GetCategoryUseCase, UpdateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        try{
            Category category = categoryMapper.toEntity(request);
            return categoryMapper.toResponse(categoryRepository.save(category));
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new CategoryAlreadyExists(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found by id: " + categoryId);
        }

        productRepository.clearCategoryForProductsByCategoryId(categoryId);

        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional
    public void deleteCategory(String name) {

        if (!categoryRepository.existsByName(name)) {
            throw new CategoryNotFoundException("Category not found by name: " + name);
        }
        productRepository.clearCategoryForProductsByCategoryName(name);

        categoryRepository.deleteByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryMapper.toResponses(categoryRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by name: " + name));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by id: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategoryByName(String name, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by name: " + name));
        categoryMapper.toEntity(category, request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategoryById(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found by id: " + id));
        categoryMapper.toEntity(category, request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }
}
