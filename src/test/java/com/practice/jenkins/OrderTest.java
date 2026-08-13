package com.practice.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void exposesTheValuesItWasBuiltWith() {
        Order order = new Order("ORD-1", "SKU-1", 3, 10.0);
        assertEquals("ORD-1", order.getId());
        assertEquals("SKU-1", order.getSku());
        assertEquals(3, order.getQuantity());
        assertEquals(10.0, order.getUnitPrice(), 0.0001);
    }

    @Test
    void calculatesSubtotal() {
        assertEquals(30.0, new Order("ORD-1", "SKU-1", 3, 10.0).getSubtotal(), 0.0001);
    }

    @Test
    void rejectsBlankIdAndSku() {
        assertThrows(IllegalArgumentException.class, () -> new Order("  ", "SKU-1", 1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Order(null, "SKU-1", 1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Order("ORD-1", "", 1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Order("ORD-1", null, 1, 1.0));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Order("ORD-1", "SKU-1", 0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Order("ORD-1", "SKU-1", -2, 1.0));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new Order("ORD-1", "SKU-1", 1, -0.01));
    }

    @Test
    void comparesOnIdOnly() {
        Order first = new Order("ORD-1", "SKU-1", 1, 10.0);
        Order sameId = new Order("ORD-1", "SKU-9", 5, 99.0);
        Order otherId = new Order("ORD-2", "SKU-1", 1, 10.0);

        assertEquals(first, sameId);
        assertEquals(first.hashCode(), sameId.hashCode());
        assertNotEquals(first, otherId);
        assertNotEquals(first, "not an order");
    }

    @Test
    void printsSomethingUseful() {
        assertTrue(new Order("ORD-1", "SKU-1", 1, 10.0).toString().contains("ORD-1"));
    }
}
