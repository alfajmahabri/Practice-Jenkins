package com.practice.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

    private static final double DELTA = 0.0001;

    private final PricingService pricing = new PricingService();

    @Test
    void noDiscountBelowTheBulkThreshold() {
        assertEquals(0.0, pricing.discountFor(new Order("ORD-1", "SKU-1", 9, 10.0)), DELTA);
    }

    @Test
    @DisplayName("the bulk discount kicks in exactly at ten units")
    void bulkDiscountAtTheThreshold() {
        // subtotal 100.00, ten percent off
        assertEquals(10.0, pricing.discountFor(new Order("ORD-1", "SKU-1", 10, 10.0)), DELTA);
    }

    @Test
    void appliesTaxAtEighteenPercent() {
        assertEquals(18.0, pricing.taxFor(100.0), DELTA);
        assertEquals(0.0, pricing.taxFor(0.0), DELTA);
    }

    @Test
    void shippingIsFreeAtTheLimit() {
        assertEquals(0.0, pricing.shippingFor(500.0), DELTA);
        assertEquals(0.0, pricing.shippingFor(750.0), DELTA);
    }

    @Test
    void shippingIsChargedBelowTheLimit() {
        assertEquals(40.0, pricing.shippingFor(498.99), DELTA);
    }

    @Test
    void totalForSmallOrder() {
        // subtotal 90.00, no discount, tax 16.20, shipping 40.00
        assertEquals(146.20, pricing.totalFor(new Order("ORD-1", "SKU-1", 2, 45.0)), DELTA);
    }

    @Test
    void totalForBulkOrderStillUnderTheShippingLimit() {
        // subtotal 540.00, discount 54.00, discounted 486.00, tax 87.48, shipping 40.00
        assertEquals(613.48, pricing.totalFor(new Order("ORD-1", "SKU-1", 12, 45.0)), DELTA);
    }

    @Test
    void totalForBulkOrderWithFreeShipping() {
        // subtotal 600.00, discount 60.00, discounted 540.00, tax 97.20, shipping 0.00
        assertEquals(637.20, pricing.totalFor(new Order("ORD-1", "SKU-1", 10, 60.0)), DELTA);
    }
}
