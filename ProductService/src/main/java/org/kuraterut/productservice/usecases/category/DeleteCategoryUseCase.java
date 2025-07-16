package org.kuraterut.productservice.usecases.category;

import org.kuraterut.productservice.model.Category;

public interface DeleteCategoryUseCase {
    void deleteCategory(Long id);
    void deleteCategory(String name);
}
