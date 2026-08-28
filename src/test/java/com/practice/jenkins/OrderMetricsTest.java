package com.practice.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class OrderMetricsTest {

    private final OrderMetrics metrics = new OrderMetrics();

    @Test
    void calculatesAcceptanceRate() {
        List<ProcessedOrder> results = results(
                ProcessedOrder.accepted(order("ORD-1"), 100.0),
                ProcessedOrder.rejected(order("ORD-2"), "out of stock"));

        assertEquals(50.0, metrics.acceptanceRate(results), 0.0001);
    }

    @Test
    void handlesEmptyAndAllRejectedResults() {
        assertEquals(0.0, metrics.acceptanceRate(List.of()), 0.0001);
        assertEquals(0.0, metrics.averageAcceptedValue(
                List.of(ProcessedOrder.rejected(order("ORD-1"), "out of stock"))), 0.0001);
    }

    @Test
    void calculatesAverageAcceptedValue() {
        List<ProcessedOrder> results = results(
                ProcessedOrder.accepted(order("ORD-1"), 100.0),
                ProcessedOrder.accepted(order("ORD-2"), 50.0));

        assertEquals(75.0, metrics.averageAcceptedValue(results), 0.0001);
    }

    private List<ProcessedOrder> results(ProcessedOrder... results) {
        return List.of(results);
    }

    private Order order(String id) {
        return new Order(id, "SKU-1", 1, 10.0);
    }
}
