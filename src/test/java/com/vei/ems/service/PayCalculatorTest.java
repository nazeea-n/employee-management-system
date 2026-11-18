package com.vei.ems.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayCalculatorTest {

    @Test
    void fullTimeBreakdownIsCorrect() {
        assertEquals(10000.0, PayCalculator.hra(50000), 0.001);
        assertEquals(5000.0, PayCalculator.da(50000), 0.001);
        assertEquals(65000.0, PayCalculator.gross(50000), 0.001);
        assertEquals(6000.0, PayCalculator.pf(50000), 0.001);
    }

    @Test
    void fullTimeNetIsGrossMinusPf() {
        assertEquals(59000.0, PayCalculator.netFullTime(50000), 0.001);
        assertEquals(47200.0, PayCalculator.netFullTime(40000), 0.001);
    }

    @Test
    void partTimeIsRateTimesHours() {
        assertEquals(24000.0, PayCalculator.netPartTime(300, 80), 0.001);
    }

    @Test
    void roundsToTwoDecimals() {
        assertEquals(1.24, PayCalculator.round(1.239), 0.0001);
    }
}
