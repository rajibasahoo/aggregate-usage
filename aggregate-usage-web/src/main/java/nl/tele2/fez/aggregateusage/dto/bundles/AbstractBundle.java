package nl.tele2.fez.aggregateusage.dto.bundles;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.Unit;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
public abstract class AbstractBundle implements Bundle {
    private final BundleType type;
    private final String name;
    private final String productName;
    private final String crmRef;
    private final Unit unit;
    private final RepeatType repeatType;
    private final LocalDateTime lastUsage;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
}
