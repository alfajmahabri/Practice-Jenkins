package com.practice.jenkins;

/**
 * Plain arithmetic helper. Kept deliberately simple so that a broken unit test
 * here points at one obvious place.
 */
public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    /**
     * Integer division.
     *
     * @throws IllegalArgumentException when the divisor is zero
     */
    public int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed");
        }
        return a / b;
    }

    /**
     * Applies a percentage to a value, rounding half up.
     *
     * @param value   the base amount
     * @param percent the percentage to apply, 0 to 100
     */
    public double percentageOf(double value, double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be between 0 and 100, got " + percent);
        }
        return value * percent / 100.0;
    }
}
