package com.yowyob.template.domain.financial;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CommissionCalculatorTest {

    @Test
    void commissionMatchesHalfUpFourDecimals() {
        BigDecimal base = new BigDecimal("100");
        BigDecimal rate = new BigDecimal("0.1");
        assertEquals(0, CommissionCalculator.commissionFromBaseAmount(base, rate).compareTo(new BigDecimal("10.0000")));
    }

    @Test
    void commissionKafkaRatePointZeroFive() {
        BigDecimal base = new BigDecimal("100");
        BigDecimal rate = new BigDecimal("0.05");
        assertEquals(0, CommissionCalculator.commissionFromBaseAmount(base, rate).compareTo(new BigDecimal("5.0000")));
    }
}
