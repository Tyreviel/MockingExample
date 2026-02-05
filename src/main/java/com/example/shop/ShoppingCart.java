package com.example.shop;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShoppingCart {
    private final Map<Product, Integer> products;
    private int totalQuantity;

    public ShoppingCart() {
        this.products = new HashMap<>();
        this.totalQuantity = 0;
    }

    public void addProduct(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Cannot add a null product to the cart.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (quantity == 0) {
            return; // Do nothing if quantity is zero
        }

        products.merge(product, quantity, Integer::sum);
        totalQuantity += quantity;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public Map<Product, Integer> getProducts() {
        return Collections.unmodifiableMap(products);
    }
}
