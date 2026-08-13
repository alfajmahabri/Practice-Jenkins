package com.practice.jenkins;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point. Runs a tiny scripted scenario so the packaged JAR does
 * something visible when a pipeline stage executes it.
 */
public final class App {

    private final InventoryService inventory = new InventoryService();
    private final PricingService pricing = new PricingService();

    public static void main(String[] args) {
        System.out.println(new App().run());
    }

    /**
     * Seeds stock, places a few orders and returns a printable summary.
     */
    public String run() {
        inventory.addStock("SKU-KEYBOARD", 25);
        inventory.addStock("SKU-MONITOR", 4);

        List<Order> orders = new ArrayList<>();
        orders.add(new Order("ORD-1", "SKU-KEYBOARD", 12, 45.0));
        orders.add(new Order("ORD-2", "SKU-MONITOR", 6, 220.0));
        orders.add(new Order("ORD-3", "SKU-KEYBOARD", 2, 45.0));

        StringBuilder summary = new StringBuilder("order-service ").append(version()).append(System.lineSeparator());
        for (Order order : orders) {
            if (inventory.reserve(order)) {
                summary.append(String.format("ACCEPTED %s total=%.2f%n", order.getId(), pricing.totalFor(order)));
            } else {
                summary.append(String.format("REJECTED %s out of stock%n", order.getId()));
            }
        }
        summary.append("remaining stock: ").append(inventory.snapshot());
        return summary.toString();
    }

    /**
     * Reads the version stamped into the JAR manifest at build time. Falls back
     * to "dev" when the classes are run straight out of target/classes.
     */
    public String version() {
        String implementationVersion = App.class.getPackage().getImplementationVersion();
        return implementationVersion == null ? "dev" : implementationVersion;
    }
}
