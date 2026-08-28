package com.practice.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class OrderWorkflowServiceTest {

    private final OrderWorkflowService workflow = new OrderWorkflowService();
    private final PricingService pricing = new PricingService();

    @Test
    void acceptsOrderAndCalculatesTotal() {
        InventoryService inventory = inventoryWith("SKU-1", 5);
        Order order = new Order("ORD-1", "SKU-1", 2, 45.0);

        ProcessedOrder result = workflow.process(order, inventory, pricing);

        assertTrue(result.isAccepted());
        assertEquals(OrderStatus.ACCEPTED, result.getStatus());
        assertEquals(146.20, result.getTotal(), 0.0001);
        assertEquals(3, inventory.getStock("SKU-1"));
    }

    @Test
    void rejectsOrderWithoutChangingInventory() {
        InventoryService inventory = inventoryWith("SKU-1", 1);
        Order order = new Order("ORD-1", "SKU-1", 2, 45.0);

        ProcessedOrder result = workflow.process(order, inventory, pricing);

        assertFalse(result.isAccepted());
        assertEquals("out of stock", result.getReason());
        assertEquals(1, inventory.getStock("SKU-1"));
    }

    @Test
    void processesBatchAndCalculatesRevenueAndCounts() {
        InventoryService inventory = inventoryWith("SKU-1", 3);
        List<Order> orders = List.of(
                new Order("ORD-1", "SKU-1", 2, 45.0),
                new Order("ORD-2", "SKU-1", 2, 45.0));

        List<ProcessedOrder> results = workflow.processAll(orders, inventory, pricing);

        assertEquals(2, results.size());
        assertEquals(1, workflow.countByStatus(results, OrderStatus.ACCEPTED));
        assertEquals(1, workflow.countByStatus(results, OrderStatus.REJECTED));
        assertEquals(146.20, workflow.acceptedRevenue(results), 0.0001);
    }

    @Test
    void returnsReadOnlyBatchResults() {
        InventoryService inventory = inventoryWith("SKU-1", 1);
        List<ProcessedOrder> results = workflow.processAll(
                List.of(new Order("ORD-1", "SKU-1", 1, 10.0)), inventory, pricing);

        assertTrue(results instanceof List);
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> results.add(results.get(0)));
    }

    private InventoryService inventoryWith(String sku, int quantity) {
        InventoryService inventory = new InventoryService();
        inventory.reserveBulk(Map.of());
        inventory.addStock(sku, quantity);
        return inventory;
    }
}
