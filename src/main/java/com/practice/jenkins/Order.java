package com.practice.jenkins;

import java.util.Objects;

/**
 * A single line item in an order.
 */
public class Order {

    private final String id;
    private final String sku;
    private final int quantity;
    private final double unitPrice;

    public Order(String id, String sku, int quantity, double unitPrice) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id must not be blank");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got " + quantity);
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price must not be negative, got " + unitPrice);
        }
        this.id = id;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getSubtotal() {
        return quantity * unitPrice;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Order)) {
            return false;
        }
        Order that = (Order) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", sku=" + sku + ", quantity=" + quantity + ", unitPrice=" + unitPrice + "}";
    }
}
