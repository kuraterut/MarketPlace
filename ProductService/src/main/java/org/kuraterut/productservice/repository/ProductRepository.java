package org.kuraterut.productservice.repository;

import org.apache.kafka.common.protocol.types.Field;
import org.kuraterut.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategoryId(Long id, Pageable pageable);
    Page<Product> findByCategoryName(String categoryName, Pageable pageable);
    Page<Product> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Product> findBySellerId(Long id, Pageable pageable);

    boolean existsByIdAndSellerId(Long id, Long userId);

    @Modifying
    @Query("UPDATE Product p SET p.category = null WHERE p.category.id = :categoryId")
    void clearCategoryForProductsByCategoryId(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE Product p SET p.category = null WHERE p.category.name = :categoryName")
    void clearCategoryForProductsByCategoryName(@Param("categoryName") String categoryName);
}
