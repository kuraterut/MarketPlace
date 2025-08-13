package unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuraterut.productservice.dto.requests.CreateProductRequest;
import org.kuraterut.productservice.dto.responses.ProductListResponse;
import org.kuraterut.productservice.dto.responses.ProductResponse;
import org.kuraterut.productservice.exception.model.CategoryNotFoundException;
import org.kuraterut.productservice.exception.model.PermissionDeniedException;
import org.kuraterut.productservice.exception.model.ProductNotFoundException;
import org.kuraterut.productservice.mapper.ProductMapper;
import org.kuraterut.productservice.model.entity.Category;
import org.kuraterut.productservice.model.entity.Product;
import org.kuraterut.productservice.repository.CategoryRepository;
import org.kuraterut.productservice.repository.ProductRepository;
import org.kuraterut.productservice.service.ProductService;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProduct_success() {
        CreateProductRequest req = new CreateProductRequest("Product1", "Description1", BigDecimal.TEN, "Category1", 1L);
        Category category = new Category();
        Product product = new Product();
        ProductResponse expectedResponse = new ProductResponse();

        when(categoryRepository.findByName("Category1")).thenReturn(Optional.of(category));
        when(productMapper.toEntity(req)).thenReturn(product);
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(expectedResponse);

        ProductResponse result = productService.createProduct(req, 1L);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(product.getSellerId()).isEqualTo(1L);
        verify(productRepository).saveAndFlush(product);
    }

    @Test
    void createProduct_categoryNotFound() {
        CreateProductRequest req = new CreateProductRequest("Product1", "Description1", BigDecimal.TEN, "Category1", 1L);
        when(categoryRepository.findByName("Category1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(req, 1L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void deleteProduct_notOwner() {
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.existsByIdAndSellerId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(1L, 2L))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void deleteProduct_notFound() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(1L, 1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.existsByIdAndSellerId(1L, 1L)).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L, 1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void getAllProducts_success() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        ProductListResponse response = new ProductListResponse(List.of());

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(productMapper.toResponses(page)).thenReturn(response);

        ProductListResponse result = productService.getAllProducts(pageable);

        assertThat(result).isEqualTo(response);
    }
}

