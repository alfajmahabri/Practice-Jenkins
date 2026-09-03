package com.practice.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PriceCacheTest {

    @Test
    void cacheDoesNotLeakPreviousPricingForSameSku() {
        PriceCache priceCache = new PriceCache();
        priceCache.clear();

        assertEquals(10.0, priceCache.calculate("SKU-1", 10.0), 0.0001);
        assertEquals(25.0, priceCache.calculate("SKU-1", 25.0), 0.0001);
    }
}
