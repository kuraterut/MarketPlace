package org.kuraterut.productservice.exception.model;

public class CategoryAlreadyExists extends RuntimeException {
    public CategoryAlreadyExists(String message) {
        super(message);
    }
}
