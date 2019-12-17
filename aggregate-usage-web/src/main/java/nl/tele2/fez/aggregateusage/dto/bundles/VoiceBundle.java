package nl.tele2.fez.aggregateusage.dto.bundles;

import lombok.Builder;
import lombok.Getter;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.Unit;
import nl.tele2.fez.aggregateusage.dto.usage.Usage;

import java.time.LocalDateTime;

@Getter
public class VoiceBundle extends AbstractBundle {
    private final Usage usage;

    @Builder
    public VoiceBundle(String name, String productName, String crmRef, RepeatType repeatType, LocalDateTime lastUsage, Usage usage, LocalDateTime startDate, LocalDateTime endDate, BundleType type, Unit unit) {
        super(type, name, productName, crmRef, unit, repeatType, lastUsage, startDate, endDate);
        this.usage = usage;
    }
}
