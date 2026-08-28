package com.practice.jenkins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Coordinates inventory reservations and order pricing.
 */
public final class OrderWorkflowService {

    public ProcessedOrder process(Order order, InventoryService inventory, PricingService pricing) {
        if (!inventory.reserve(order)) {
            return ProcessedOrder.rejected(order, "out of stock");
        }
        return ProcessedOrder.accepted(order, pricing.totalFor(order));
    }

    public List<ProcessedOrder> processAll(List<Order> orders, InventoryService inventory,
            PricingService pricing) {
        List<ProcessedOrder> results = new ArrayList<>();
        for (Order order : orders) {
            results.add(process(order, inventory, pricing));
        }
        return Collections.unmodifiableList(results);
    }

    public double acceptedRevenue(List<ProcessedOrder> results) {
        double revenue = 0.0;
        for (ProcessedOrder result : results) {
            revenue += result.getTotal();
        }
        return revenue;
    }

    public long countByStatus(List<ProcessedOrder> results, OrderStatus status) {
        return results.stream().filter(ProcessedOrder::isAccepted).count();
    }
}
