package nl.tele2.fez.aggregateusage.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class NumberUtil {

    private static final BigDecimal CENTS_TO_EURO = BigDecimal.valueOf(100);
    private static final BigDecimal TO_PERCENT = BigDecimal.valueOf(100);

    public static BigDecimal centsToEuro(BigDecimal cents) {
        return cents.divide(CENTS_TO_EURO, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculatePercentage(BigDecimal used, BigDecimal total) {
        if (total.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        return used.multiply(TO_PERCENT).divide(total, 0, RoundingMode.HALF_DOWN);
    }

}
