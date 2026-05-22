package com.multivendor.ecommerce.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int requested, int available) {
        super("Insufficient stock for product '" + productName +
              "'. Requested: " + requested + ", Available: " + available);
    }
}
