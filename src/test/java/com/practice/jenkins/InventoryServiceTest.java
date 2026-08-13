package com.practice.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {

    private InventoryService inventory;

    @BeforeEach
    void setUp() {
        inventory = new InventoryService();
        inventory.addStock("SKU-1", 10);
    }

    @Test
    void reportsZeroForUnknownSku() {
        assertEquals(0, inventory.getStock("SKU-UNKNOWN"));
    }

    @Test
    void accumulatesStockForTheSameSku() {
        inventory.addStock("SKU-1", 5);
        assertEquals(15, inventory.getStock("SKU-1"));
    }

    @Test
    void rejectsNonPositiveStock() {
        assertThrows(IllegalArgumentException.class, () -> inventory.addStock("SKU-1", 0));
        assertThrows(IllegalArgumentException.class, () -> inventory.addStock("SKU-1", -3));
    }

    @Test
    void checksAvailabilityAtTheBoundary() {
        assertTrue(inventory.isAvailable("SKU-1", 10));
        assertFalse(inventory.isAvailable("SKU-1", 11));
    }

    @Test
    void reserveRemovesStock() {
        assertTrue(inventory.reserve(new Order("ORD-1", "SKU-1", 4, 10.0)));
        assertEquals(6, inventory.getStock("SKU-1"));
    }

    @Test
    void reserveFailsWithoutTouchingStockWhenShort() {
        assertFalse(inventory.reserve(new Order("ORD-1", "SKU-1", 11, 10.0)));
        assertEquals(10, inventory.getStock("SKU-1"));
    }

    @Test
    void releasePutsStockBack() {
        Order order = new Order("ORD-1", "SKU-1", 4, 10.0);
        inventory.reserve(order);
        inventory.release(order);
        assertEquals(10, inventory.getStock("SKU-1"));
    }

    @Test
    void snapshotIsReadOnly() {
        assertEquals(10, inventory.snapshot().get("SKU-1"));
        assertThrows(UnsupportedOperationException.class, () -> inventory.snapshot().put("SKU-2", 1));
    }
}
