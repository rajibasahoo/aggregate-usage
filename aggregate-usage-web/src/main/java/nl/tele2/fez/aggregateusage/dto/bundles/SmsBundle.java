package nl.tele2.fez.aggregateusage.dto.bundles;

import lombok.Builder;
import lombok.Getter;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.Unit;
import nl.tele2.fez.aggregateusage.dto.usage.Usage;

import java.time.LocalDateTime;

@Getter
public class SmsBundle extends AbstractBundle {
    private final Usage usage;

    @Builder
    public SmsBundle(String name, String productName, String crmRef, RepeatType repeatType, LocalDateTime lastUsage, Usage usage, LocalDateTime startDate, LocalDateTime endDate) {
        super(BundleType.SMS, name, productName, crmRef, Unit.SMS, repeatType, lastUsage, startDate, endDate);
        this.usage = usage;
    }
}
