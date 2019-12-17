package nl.tele2.fez.aggregateusage.dto.usage;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class FairUsePolicyUsage extends LimitedUsage {
    public FairUsePolicyUsage(BigDecimal total, BigDecimal remaining) {
        super(total, remaining);
    }

    @Override
    public boolean isUnlimited() {
        return true;
    }
}
