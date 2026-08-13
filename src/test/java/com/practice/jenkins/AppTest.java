package com.practice.jenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppTest {

    private App app;
    private String summary;

    @BeforeEach
    void setUp() {
        app = new App();
        summary = app.run();
    }

    @Test
    void acceptsOrdersThatHaveStock() {
        assertTrue(summary.contains("ACCEPTED ORD-1"), summary);
        assertTrue(summary.contains("ACCEPTED ORD-3"), summary);
    }

    @Test
    void rejectsTheOrderThatIsShortOnStock() {
        assertTrue(summary.contains("REJECTED ORD-2"), summary);
    }

    @Test
    void reportsRemainingStock() {
        assertTrue(summary.contains("SKU-KEYBOARD=11"), summary);
        assertTrue(summary.contains("SKU-MONITOR=4"), summary);
    }

    @Test
    void fallsBackToDevVersionOutsideAPackagedJar() {
        assertEquals("dev", app.version());
    }

    @Test
    void mainRunsWithoutBlowingUp() {
        App.main(new String[0]);
    }
}
