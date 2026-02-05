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

    // ========== Remove product tests ==========

    @Test
    @DisplayName("should remove a product completely from the cart")
    void shouldRemoveProductCompletely() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 3);
        cart.removeProduct(product, 3);
        assertThat(cart.getTotalQuantity()).isEqualTo(0);
        assertThat(cart.getProducts()).doesNotContainKey(product);
    }

    @Test
    @DisplayName("should remove a partial quantity of a product from the cart")
    void shouldRemovePartialQuantityOfProduct() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 5);
        cart.removeProduct(product, 2);
        assertThat(cart.getTotalQuantity()).isEqualTo(3);
        assertThat(cart.getProducts()).containsEntry(product, 3);
    }

    @Test
    @DisplayName("should remove product completely if quantity to remove equals current quantity")
    void shouldRemoveProductCompletelyIfQuantityEquals() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 5);
        cart.removeProduct(product, 5);
        assertThat(cart.getTotalQuantity()).isEqualTo(0);
        assertThat(cart.getProducts()).doesNotContainKey(product);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when attempting to remove more than available quantity")
    void shouldThrowExceptionWhenRemovingMoreThanAvailable() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 3);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            cart.removeProduct(product, 5);
        });
        assertThat(cart.getTotalQuantity()).isEqualTo(3);
        assertThat(cart.getProducts()).containsEntry(product, 3);
    }

    @Test
    @DisplayName("should do nothing when attempting to remove a product not in the cart")
    void shouldDoNothingWhenRemovingNonExistentProduct() {
        Product product1 = new Product("P001", "Laptop", 1200.00);
        Product product2 = new Product("P002", "Mouse", 25.00);
        cart.addProduct(product1, 1);
        cart.removeProduct(product2, 1); // product2 is not in cart
        assertThat(cart.getTotalQuantity()).isEqualTo(1);
        assertThat(cart.getProducts()).containsEntry(product1, 1);
        assertThat(cart.getProducts()).doesNotContainKey(product2);
    }

    // Edge case: Removing null product or invalid quantity
    @Test
    @DisplayName("should throw IllegalArgumentException when removing a null product")
    void shouldThrowExceptionWhenRemovingNullProduct() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            cart.removeProduct(null, 1);
        });
        assertThat(cart.getTotalQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when removing product with negative quantity")
    void shouldThrowExceptionWhenRemovingProductWithNegativeQuantity() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 1);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            cart.removeProduct(product, -1);
        });
        assertThat(cart.getTotalQuantity()).isEqualTo(1);
        assertThat(cart.getProducts()).containsEntry(product, 1);
    }

    @Test
    @DisplayName("should do nothing when removing product with zero quantity")
    void shouldDoNothingWhenRemovingProductWithZeroQuantity() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 5);
        cart.removeProduct(product, 0);
        assertThat(cart.getTotalQuantity()).isEqualTo(5);
        assertThat(cart.getProducts()).containsEntry(product, 5);
    }
}
