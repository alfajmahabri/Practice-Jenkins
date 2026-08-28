package com.practice.jenkins;

import java.util.List;

/**
 * Calculates operational metrics from processed orders.
 */
public final class OrderMetrics {

    public double acceptanceRate(List<ProcessedOrder> results) {
        if (results.isEmpty()) {
            return 0.0;
        }
        long accepted = results.stream().filter(ProcessedOrder::isAccepted).count();
        return accepted * 100.0 / results.size();
    }

    public double averageAcceptedValue(List<ProcessedOrder> results) {
        long accepted = results.stream().filter(ProcessedOrder::isAccepted).count();
        if (accepted == 0) {
            return 0.0;
        }
        double revenue = results.stream().filter(ProcessedOrder::isAccepted)
                .mapToDouble(ProcessedOrder::getTotal).sum();
        return revenue / accepted;
    }
}
