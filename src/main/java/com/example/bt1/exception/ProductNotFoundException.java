package com.example.bt1.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product not found: " + productId);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
