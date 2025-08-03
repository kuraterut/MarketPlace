package org.kuraterut.productservice.usecases.category;

public interface DeleteCategoryUseCase {
    void deleteCategory(Long id);
    void deleteCategory(String name);
}
