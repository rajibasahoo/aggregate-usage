package nl.tele2.fez.aggregateusage.dto.usage;

import lombok.Getter;

@Getter
public class UnlimitedUsage implements Usage {
    @Override
    public boolean isUnlimited() {
        return true;
    }

    @Override
    public Usage aggregate(Usage other) {
        // nothing to aggregate, so just return the other bundle
        return other;
    }
}
