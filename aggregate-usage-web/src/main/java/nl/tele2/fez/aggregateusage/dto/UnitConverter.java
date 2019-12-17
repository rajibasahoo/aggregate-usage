package nl.tele2.fez.aggregateusage.dto;

import lombok.experimental.UtilityClass;
import nl.tele2.fez.aggregateusage.ErrorCode;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

@UtilityClass
public class UnitConverter {
    private static final long KILO_FACTOR = 1024;
    private static final long MEGA_FACTOR = KILO_FACTOR * KILO_FACTOR;

    private static final int BYTES = 0;
    private static final int KILO_BYTES = 1;
    private static final int MEGA_BYTES = 2;
    private static final int GIGA_BYTES = 3;

    public static BigDecimal convertToMegaBytesBinary(Long value, int unit) {
        switch (unit) {
            case BYTES:
                value /= MEGA_FACTOR;
                break;
            case KILO_BYTES:
                value /= KILO_FACTOR;
                break;
            case MEGA_BYTES:
                break;
            case GIGA_BYTES:
                value *= KILO_FACTOR;
                break;
            default:
                throw new AggregateUsageException("Unknown capacityUnit " + unit, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN_CAPACITY_UNIT);
        }
        return BigDecimal.valueOf(value);
    }
}
