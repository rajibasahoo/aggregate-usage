package nl.tele2.fez.aggregateusage.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class NumberUtilTest {

    @Test
    public void toPercent() {
        assertEquals(BigDecimal.valueOf(33), NumberUtil.calculatePercentage(BigDecimal.valueOf(3), BigDecimal.valueOf(9)));
    }

    @Test
    public void centsToEuro() {
        assertEquals(BigDecimal.valueOf(33.34), NumberUtil.centsToEuro(BigDecimal.valueOf(3333.9)));
    }

    @Test
    public void shouldReturnZeroWhenTotalIsZero() {
        assertEquals(BigDecimal.ZERO, NumberUtil.calculatePercentage(BigDecimal.ONE, BigDecimal.ZERO));
    }


}
