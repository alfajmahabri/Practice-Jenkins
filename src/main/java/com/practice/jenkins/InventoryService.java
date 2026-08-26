package com.practice.jenkins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory stock tracking. No database on purpose, the point of this project
 * is the pipeline around it rather than the code itself.
 */
public class InventoryService {

    private final Map<String, Integer> stock = new HashMap<>();

    public void addStock(String sku, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock added must be positive, got " + quantity);
        }
        stock.merge(sku, quantity, Integer::sum);
    }

    public int getStock(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    public boolean isAvailable(String sku, int quantity) {
        return getStock(sku) >= quantity;
    }

    /**
     * Removes stock for an order.
     *
     * @return true when the reservation succeeded, false when stock was short
     */
    public boolean reserve(Order order) {
        if (!isAvailable(order.getSku(), order.getQuantity())) {
            return false;
        }
        stock.put(order.getSku(), getStock(order.getSku()) - order.getQuantity());
        return true;
    }

    /**
     * Puts stock back, for example after a cancelled order.
     */
    public void release(Order order) {
        stock.merge(order.getSku(), order.getQuantity(), Integer::sum);
    }

    public Map<String, Integer> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(stock));
    }

    /**
     * Removes all stock from inventory. Useful for resetting state between
     * test runs or after a bulk operation.
     */
    public void clear() {
        stock.clear();
    }

    /**
     * Reserves multiple units across different SKUs in a single call.
     * Rolls back all reservations if any single SKU has insufficient stock.
     *
     * @return a map of SKU to quantity actually reserved, empty when nothing succeeded
     */
    public Map<String, Integer> reserveBulk(Map<String, Integer> items) {
        Map<String, Integer> reserved = new HashMap<>();
        Map<String, Integer> snapshot = new HashMap<>(stock);
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String sku = entry.getKey();
            int qty = entry.getValue();
            if (isAvailable(sku, qty)) {
                stock.put(sku, getStock(sku) - qty);
                reserved.put(sku, qty);
            } else {
                for (Map.Entry<String, Integer> rollback : reserved.entrySet()) {
                    stock.put(rollback.getKey(), getStock(rollback.getKey()) + rollback.getValue());
                }
                return Collections.emptyMap();
            }
        }
        return reserved;
    }
}
