package nl.tele2.fez.aggregateusage.dto;

import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


public class UnitConverterTest {

    @Test
    public void shouldConvertKiloBytesToMegaBytes() {
        BigDecimal result = UnitConverter.convertToMegaBytesBinary(102400L, 1);
        assertThat(result).isEqualTo(new BigDecimal(100));
    }

    @Test
    public void shouldConvertBytesToMegaBytes() {
        BigDecimal result = UnitConverter.convertToMegaBytesBinary(1048576000L, 0);
        assertThat(result).isEqualTo(new BigDecimal(1000));
    }

    @Test
    public void shouldConvertGigaBytesToMegaBytes() {
        BigDecimal result = UnitConverter.convertToMegaBytesBinary(1L, 3);
        assertThat(result).isEqualTo(new BigDecimal(1024));
    }

    @Test
    public void shouldReturnSameWhenMegabytes() {
        BigDecimal result = UnitConverter.convertToMegaBytesBinary(100L, 2);
        assertThat(result).isEqualTo(new BigDecimal(100));
    }

    @Test(expected = AggregateUsageException.class)
    public void shouldThrowExceptionWhenUnknownUnit() {
        UnitConverter.convertToMegaBytesBinary(100L, 999);
    }

}