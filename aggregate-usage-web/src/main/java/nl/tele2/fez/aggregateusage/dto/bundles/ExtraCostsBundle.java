package nl.tele2.fez.aggregateusage.dto.bundles;

import lombok.Builder;
import lombok.Getter;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.Unit;
import nl.tele2.fez.aggregateusage.dto.usage.MoneyUsage;
import nl.tele2.fez.aggregateusage.dto.usage.Usage;

import java.time.LocalDateTime;

@Getter
public class ExtraCostsBundle extends AbstractBundle {
    private final Usage usage;

    @Builder
    public ExtraCostsBundle(String name, String productName, String crmRef, RepeatType repeatType, LocalDateTime lastUsage, MoneyUsage usage, LocalDateTime startDate, LocalDateTime endDate) {
        super(BundleType.EXTRA_COSTS, name, productName, crmRef, Unit.MONEY, repeatType, lastUsage, startDate, endDate);
        this.usage = usage;
    }
}
