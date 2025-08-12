package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.requests.UpdateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.exception.model.CategoryAlreadyExistsException;
import org.kuraterut.productservice.exception.model.CategoryNotFoundException;
import org.kuraterut.productservice.mapper.CategoryMapper;
import org.kuraterut.productservice.model.entity.Category;
import org.kuraterut.productservice.repository.CategoryRepository;
import org.kuraterut.productservice.repository.ProductRepository;
import org.kuraterut.productservice.service.CategoryService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private final OffsetDateTime testCreatedAtDateTime = OffsetDateTime.of(2025, 8, 12, 0, 0, 0, 0, ZoneOffset.ofHours(1));
    private final OffsetDateTime testUpdatedAtDateTime = OffsetDateTime.of(2025, 8, 12, 0, 0, 0, 0, ZoneOffset.ofHours(1));

    Category getTestCategory() {
        return new Category(
                1L,
                "categoryTestName",
                "categoryTestDescription",
                testCreatedAtDateTime,
                testUpdatedAtDateTime,
                new HashSet<>()
        );
    }

    @Test
    void createCategory_success() {
        CreateCategoryRequest request = new CreateCategoryRequest("categoryTestName", "categoryTestDescription");
        Category category = getTestCategory();
        CategoryResponse response = new CategoryResponse(1L,
                "categoryTestName",
                "categoryTestDescription",
                testCreatedAtDateTime.toString(),
                testUpdatedAtDateTime.toString(),
                new HashSet<>());

        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.saveAndFlush(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result).isEqualTo(response);
        verify(categoryRepository).saveAndFlush(category);
    }

    @Test
    void createCategory_alreadyExists_throwsException() {
        CreateCategoryRequest request = new CreateCategoryRequest("categoryTestName", "categoryTestDescription");

        when(categoryMapper.toEntity(request)).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(CategoryAlreadyExistsException.class);
    }

    @Test
    void deleteCategoryById_success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(productRepository).clearCategoryForProductsByCategoryId(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategoryById_notFound_throwsException() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void getCategoryByName_success() {
        Category category = getTestCategory();
        CategoryResponse response = new CategoryResponse(1L,
                "categoryTestName",
                "categoryTestDescription",
                testCreatedAtDateTime.toString(),
                testUpdatedAtDateTime.toString(),
                new HashSet<>());

        when(categoryRepository.findByName("categoryTestName")).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.getCategory("categoryTestName");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void updateCategoryById_success() {
        Category category = getTestCategory();
        UpdateCategoryRequest request = new UpdateCategoryRequest("NewName", "NewDescription");
        CategoryResponse response = new CategoryResponse(1L,
                "NewName",
                "NewDescription",
                testCreatedAtDateTime.toString(),
                testUpdatedAtDateTime.toString(),
                new HashSet<>());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("NewName")).thenReturn(false);
        when(categoryRepository.saveAndFlush(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.updateCategoryById(1L, request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void updateCategoryById_nameConflict_throwsException() {
        Category category = getTestCategory();
        UpdateCategoryRequest request = new UpdateCategoryRequest("ExistingName", "ExistingDescription");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("ExistingName")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategoryById(1L, request))
                .isInstanceOf(CategoryAlreadyExistsException.class);
    }
}
