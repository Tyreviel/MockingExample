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

        int currentQuantity = products.getOrDefault(product, 0);
        products.remove(product); // Remove the old entry
        products.put(product, currentQuantity + quantity); // Add the new product with updated details and summed quantity
        totalQuantity += quantity;
    }

    public void removeProduct(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Cannot remove a null product from the cart.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (quantity == 0) {
            return; // Do nothing if quantity is zero
        }

        if (!products.containsKey(product)) {
            return; // Product not in cart, do nothing
        }

        int currentQuantity = products.get(product);
        if (quantity > currentQuantity) {
            throw new IllegalArgumentException("Cannot remove more products than are in the cart. Available: " + currentQuantity + ", Attempted to remove: " + quantity);
        }

        int newQuantity = currentQuantity - quantity;
        if (newQuantity <= 0) {
            products.remove(product);
        } else {
            products.put(product, newQuantity);
        }
        totalQuantity -= quantity;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public Map<Product, Integer> getProducts() {
        return Collections.unmodifiableMap(products);
    }

    public double calculateTotalPrice() {
        double totalPrice = 0.0;
        for (Map.Entry<Product, Integer> entry : products.entrySet()) {
            totalPrice += entry.getKey().getPrice() * entry.getValue();
        }
        return totalPrice;
    }

    public double applyPercentageDiscount(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }

        double currentTotalPrice = calculateTotalPrice();
        if (currentTotalPrice == 0.0) {
            return 0.0;
        }

        double discountAmount = currentTotalPrice * (percentage / 100.0);
        double discountedPrice = currentTotalPrice - discountAmount;

        // Ensure discounted price is not negative
        return Math.max(0.0, discountedPrice);
    }

    public void clear() {
        products.clear();
        totalQuantity = 0;
    }

    public void updateProductQuantity(Product product, int newQuantity) {
        if (product == null) {
            throw new IllegalArgumentException("Cannot update quantity for a null product.");
        }
        if (newQuantity < 0) {
            throw new IllegalArgumentException("New quantity cannot be negative.");
        }

        int currentQuantityInCart = products.getOrDefault(product, 0);

        if (newQuantity == 0) {
            if (products.containsKey(product)) {
                products.remove(product);
                totalQuantity -= currentQuantityInCart;
            }
        } else { // newQuantity > 0
            products.put(product, newQuantity); // This will update the product details if key already exists, and set new quantity
            if (currentQuantityInCart == 0) { // Product was not in cart, add new quantity
                totalQuantity += newQuantity;
            } else { // Product was in cart, update quantity difference
                totalQuantity += (newQuantity - currentQuantityInCart);
            }
        }
    }
}