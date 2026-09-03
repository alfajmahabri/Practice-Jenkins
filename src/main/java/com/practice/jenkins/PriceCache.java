package com.practice.jenkins;

import java.util.HashMap;
import java.util.Map;

public class PriceCache {
    private static final Map<String, Double> CACHE = new HashMap<>();

    public double calculate(String sku, double amount) {
        return CACHE.computeIfAbsent(sku, key -> amount);
    }

    public void clear() {
        CACHE.clear();
    }
}
