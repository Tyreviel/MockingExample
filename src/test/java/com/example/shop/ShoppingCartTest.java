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

    // ========== Calculate Total Price tests ==========

    @Test
    @DisplayName("should return 0.0 for an empty cart total price")
    void shouldReturnZeroForEmptyCartTotalPrice() {
        assertThat(cart.calculateTotalPrice()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should calculate total price for a single product with single quantity")
    void shouldCalculateTotalPriceForSingleProductSingleQuantity() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 1);
        assertThat(cart.calculateTotalPrice()).isEqualTo(1200.00);
    }

    @Test
    @DisplayName("should calculate total price for a single product with multiple quantities")
    void shouldCalculateTotalPriceForSingleProductMultipleQuantity() {
        Product product = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(product, 3);
        assertThat(cart.calculateTotalPrice()).isEqualTo(3600.00);
    }

    @Test
    @DisplayName("should calculate total price for multiple products with multiple quantities")
    void shouldCalculateTotalPriceForMultipleProductsMultipleQuantity() {
        Product product1 = new Product("P001", "Laptop", 1200.00);
        Product product2 = new Product("P002", "Mouse", 25.00);
        Product product3 = new Product("P003", "Keyboard", 75.50);
        cart.addProduct(product1, 1); // 1200.00
        cart.addProduct(product2, 2); // 50.00
        cart.addProduct(product3, 1); // 75.50
        assertThat(cart.calculateTotalPrice()).isEqualTo(1200.00 + 50.00 + 75.50);
    }

    @Test
    @DisplayName("should handle products with zero price correctly")
    void shouldHandleProductsWithZeroPrice() {
        Product product1 = new Product("P001", "Freebie", 0.00);
        Product product2 = new Product("P002", "Paid Item", 100.00);
        cart.addProduct(product1, 5);
        cart.addProduct(product2, 1);
        assertThat(cart.calculateTotalPrice()).isEqualTo(100.00);
    }

    @Test
    @DisplayName("should handle products with decimal prices correctly")
    void shouldHandleProductsWithDecimalPrices() {
        Product product = new Product("P004", "Book", 19.99);
        cart.addProduct(product, 2);
        assertThat(cart.calculateTotalPrice()).isEqualTo(39.98);
    }

    // ========== Apply Percentage Discount tests ==========

    @Test
    @DisplayName("should apply a percentage discount to the total price")
    void shouldApplyPercentageDiscount() {
        Product product1 = new Product("P001", "Laptop", 1000.00);
        Product product2 = new Product("P002", "Mouse", 100.00);
        cart.addProduct(product1, 1);
        cart.addProduct(product2, 1); // Total: 1100.00
        double discountedPrice = cart.applyPercentageDiscount(10.0); // 10% discount
        assertThat(discountedPrice).isEqualTo(990.00); // 1100 * 0.9 = 990
    }

    @Test
    @DisplayName("should apply a zero percentage discount with no change in total price")
    void shouldApplyZeroPercentageDiscount() {
        Product product1 = new Product("P001", "Laptop", 1000.00);
        cart.addProduct(product1, 1); // Total: 1000.00
        double discountedPrice = cart.applyPercentageDiscount(0.0);
        assertThat(discountedPrice).isEqualTo(1000.00);
    }

    @Test
    @DisplayName("should cap total price at 0.0 when discount is 100%")
    void shouldCapTotalPriceAtZeroWhenDiscountIsOneHundredPercent() {
        Product product1 = new Product("P001", "Cheap Item", 10.00);
        cart.addProduct(product1, 1); // Total: 10.00
        double discountedPrice = cart.applyPercentageDiscount(100.0); // 100% discount
        assertThat(discountedPrice).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should return 0.0 when applying discount to an empty cart")
    void shouldReturnZeroWhenApplyingDiscountToEmptyCart() {
        double discountedPrice = cart.applyPercentageDiscount(10.0);
        assertThat(discountedPrice).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for negative discount percentage")
    void shouldThrowExceptionForNegativeDiscountPercentage() {
        Product product = new Product("P001", "Laptop", 1000.00);
        cart.addProduct(product, 1);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            cart.applyPercentageDiscount(-5.0);
        });
    }

    @Test
    @DisplayName("should completely clear the cart")
    void shouldClearCart() {
        Product product1 = new Product("P001", "Laptop", 1200.00);
        Product product2 = new Product("P002", "Mouse", 25.00);
        cart.addProduct(product1, 1);
        cart.addProduct(product2, 2);

        cart.clear();

        assertThat(cart.getTotalQuantity()).isEqualTo(0);
        assertThat(cart.getProducts()).isEmpty();
    }

    @Test
    @DisplayName("should update product details when adding existing product with different name or price")
    void shouldUpdateProductDetailsWhenAddingExistingProductWithDifferentNameOrPrice() {
        Product originalProduct = new Product("P001", "Laptop", 1200.00);
        cart.addProduct(originalProduct, 1);

        Product updatedProduct = new Product("P001", "Gaming Laptop", 1500.00);
        cart.addProduct(updatedProduct, 1);

        assertThat(cart.getTotalQuantity()).isEqualTo(2);
        assertThat(cart.getProducts()).hasSize(1);
        
        Product productInCart = cart.getProducts().keySet().iterator().next();
        assertThat(productInCart.getName()).isEqualTo(updatedProduct.getName()); // This will now fail
        assertThat(productInCart.getPrice()).isEqualTo(updatedProduct.getPrice()); // This will now fail
        assertThat(cart.getProducts().get(productInCart)).isEqualTo(2);
    }
}
