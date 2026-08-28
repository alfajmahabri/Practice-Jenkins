package com.practice.jenkins;

import java.util.Objects;

/**
 * Result of attempting to process one order.
 */
public final class ProcessedOrder {

    private final Order order;
    private final OrderStatus status;
    private final double total;
    private final String reason;

    private ProcessedOrder(Order order, OrderStatus status, double total, String reason) {
        this.order = Objects.requireNonNull(order);
        this.status = Objects.requireNonNull(status);
        this.total = total;
        this.reason = Objects.requireNonNull(reason);
    }

    public static ProcessedOrder accepted(Order order, double total) {
        return new ProcessedOrder(order, OrderStatus.ACCEPTED, total, "");
    }

    public static ProcessedOrder rejected(Order order, String reason) {
        return new ProcessedOrder(order, OrderStatus.REJECTED, 0.0, reason);
    }

    public Order getOrder() {
        return order;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public double getTotal() {
        return total;
    }

    public String getReason() {
        return reason;
    }

    public boolean isAccepted() {
        return status == OrderStatus.ACCEPTED;
    }
}
