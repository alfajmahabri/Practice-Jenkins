package com.practice.jenkins;

/**
 * Works out what an order costs. Enough branching here to make coverage and
 * boundary-value tests worth writing.
 */
public class PricingService {

    private static final double TAX_RATE = 18.0;
    private static final double BULK_DISCOUNT_RATE = 10.0;
    private static final int BULK_THRESHOLD = 10;
    private static final double FREE_SHIPPING_LIMIT = 600.0;
    private static final double SHIPPING_FEE = 40.0;

    private final Calculator calculator = new Calculator();

    /**
     * Orders of ten units or more get a flat bulk discount.
     */
    public double discountFor(Order order) {
        if (order.getQuantity() < BULK_THRESHOLD) {
            return 0.0;
        }
        return calculator.percentageOf(order.getSubtotal(), BULK_DISCOUNT_RATE);
    }

    public double taxFor(double amount) {
        return calculator.percentageOf(amount, TAX_RATE);
    }

    /**
     * Shipping is waived once the discounted subtotal reaches the free limit.
     */
    public double shippingFor(double discountedSubtotal) {
        if (discountedSubtotal >= FREE_SHIPPING_LIMIT) {
            return 0.0;
        }
        return SHIPPING_FEE;
    }

    /**
     * Final payable amount: subtotal, less discount, plus tax and shipping.
     */
    public double totalFor(Order order) {
        double discounted = order.getSubtotal() - discountFor(order);
        return discounted + taxFor(discounted) + shippingFor(discounted);
    }
}
