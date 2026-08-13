package com.practice.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculatorTest {

    private final Calculator calculator = new Calculator();

    @ParameterizedTest
    @CsvSource({"2, 3, 5", "-1, 1, 0", "0, 0, 0", "-4, -6, -10"})
    void addsTwoNumbers(int a, int b, int expected) {
        assertEquals(expected, calculator.add(a, b));
    }

    @Test
    void subtractsTwoNumbers() {
        assertEquals(4, calculator.subtract(10, 6));
        assertEquals(-4, calculator.subtract(6, 10));
    }

    @Test
    void multipliesTwoNumbers() {
        assertEquals(42, calculator.multiply(6, 7));
        assertEquals(0, calculator.multiply(6, 0));
    }

    @Test
    void dividesTwoNumbers() {
        assertEquals(5, calculator.divide(10, 2));
    }

    @Test
    @DisplayName("divide by zero is rejected rather than throwing ArithmeticException")
    void rejectsDivisionByZero() {
        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> calculator.divide(10, 0));
        assertEquals("Division by zero is not allowed", thrown.getMessage());
    }

    @Test
    void calculatesPercentage() {
        assertEquals(25.0, calculator.percentageOf(100.0, 25.0), 0.0001);
        assertEquals(0.0, calculator.percentageOf(100.0, 0.0), 0.0001);
        assertEquals(100.0, calculator.percentageOf(100.0, 100.0), 0.0001);
    }

    @ParameterizedTest
    @CsvSource({"-1", "101"})
    void rejectsPercentageOutOfRange(double percent) {
        assertThrows(IllegalArgumentException.class, () -> calculator.percentageOf(100.0, percent));
    }
}
