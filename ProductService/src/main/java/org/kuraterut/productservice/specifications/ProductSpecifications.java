package org.kuraterut.productservice.specifications;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.kuraterut.productservice.model.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {
    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(name)) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasCategory(List<Long> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null) return null;
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Product> minPrice(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) return null;
            return cb.and(cb.ge(root.get("price"), minPrice));
        };
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) return null;
            return cb.and(cb.le(root.get("price"), maxPrice));
        };
    }

    public static Specification<Product> inStockOnly(Boolean inStockOnly) {
        return (root, query, cb) -> {
            if (inStockOnly == null || !inStockOnly) return null;
            return cb.greaterThan(root.get("stock"), 0);
        };
    }
}
