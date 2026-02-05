package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    @DisplayName("should add a single product to the cart")
    void shouldAddProductToCart() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 1);
        assertThat(cart.getTotalQuantity()).isEqualTo(1);
        assertThat(cart.getProducts()).containsEntry(product, 1);
    }

    @Test
    @DisplayName("should add multiple units of the same product to the cart")
    void shouldAddMultipleUnitsOfSameProduct() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 3);
        assertThat(cart.getTotalQuantity()).isEqualTo(3);
        assertThat(cart.getProducts()).containsEntry(product, 3);
    }

    @Test
    @DisplayName("should add different products to the cart")
    void shouldAddDifferentProducts() {
        Product product1 = new Product("P001", "Laptop", 1200.00);
        Product product2 = new Product("P002", "Mouse", 25.00);
        cart.addProduct(product1, 1);
        cart.addProduct(product2, 2);
        assertThat(cart.getTotalQuantity()).isEqualTo(3);
        assertThat(cart.getProducts()).containsEntry(product1, 1)
                                      .containsEntry(product2, 2);
    }

    @Test
    @DisplayName("should increase quantity when adding an existing product")
    void shouldIncreaseQuantityWhenAddingExistingProduct() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 1);
        cart.addProduct(product, 2);
        assertThat(cart.getTotalQuantity()).isEqualTo(3);
        assertThat(cart.getProducts()).containsEntry(product, 3);
    }

    // Edge case: Adding zero or negative quantity
    @Test
    @DisplayName("should not add product when quantity is zero")
    void shouldNotAddProductWhenQuantityIsZero() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 0);
        assertThat(cart.getTotalQuantity()).isEqualTo(0);
        assertThat(cart.getProducts()).doesNotContainKey(product);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when adding product with negative quantity")
    void shouldThrowExceptionWhenAddingProductWithNegativeQuantity() {
        Product product = new Product("P001", "Laptop", 1200.00);
        // Using assertThrows for expected exceptions
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            cart.addProduct(product, -1);
        });
        assertThat(cart.getTotalQuantity()).isEqualTo(0);
        assertThat(cart.getProducts()).doesNotContainKey(product);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when adding a null product")
    void shouldThrowExceptionWhenAddingNullProduct() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            cart.addProduct(null, 1);
        });
        assertThat(cart.getTotalQuantity()).isEqualTo(0);
    }
}
