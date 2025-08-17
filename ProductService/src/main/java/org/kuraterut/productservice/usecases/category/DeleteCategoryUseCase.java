package org.kuraterut.productservice.usecases.category;

public interface DeleteCategoryUseCase {
    void deleteCategoryById(Long id);
    void deleteCategoryByName(String name);
}
