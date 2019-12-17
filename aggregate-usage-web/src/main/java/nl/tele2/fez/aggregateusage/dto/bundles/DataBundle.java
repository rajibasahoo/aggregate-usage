package nl.tele2.fez.aggregateusage.dto.bundles;

import lombok.Getter;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.Unit;
import nl.tele2.fez.aggregateusage.dto.usage.Usage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class DataBundle extends AbstractBundle {
    private final Usage usage;
    private final String zone;
    private final String entitlementId;
    private final BigDecimal recurrenceAmount;

    public DataBundle(String name, String productName, String crmRef, RepeatType repeatType, LocalDateTime lastUsage, Usage usage, LocalDateTime startDate,
                      LocalDateTime endDate, String zone, String entitlementId, BigDecimal recurrenceAmount, BundleType type) {
        super(type, name, productName, crmRef, Unit.MEGABYTES, repeatType, lastUsage, startDate, endDate);
        this.usage = usage;
        this.zone = zone;

        this.entitlementId = entitlementId;
        this.recurrenceAmount = recurrenceAmount;
    }
}
