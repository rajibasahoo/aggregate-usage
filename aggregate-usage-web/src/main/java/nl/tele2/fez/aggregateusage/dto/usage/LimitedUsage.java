package nl.tele2.fez.aggregateusage.dto.usage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static nl.tele2.fez.aggregateusage.util.NumberUtil.calculatePercentage;

@Getter
public class LimitedUsage implements Usage {
    private static final BigDecimal TO_PERCENT = new BigDecimal(100);
    @ApiModelProperty(value = "Bundle limit in mb")
    private final BigDecimal limit;
    @ApiModelProperty(value = "Usage in mb")
    private final BigDecimal used;
    @ApiModelProperty(value = "Remaining amount in mb")
    private final BigDecimal remaining;
    @ApiModelProperty(value = "Percentage that is used")
    private final BigDecimal usedPercent;

    public LimitedUsage(BigDecimal total, BigDecimal remaining) {
        this.limit = total.setScale(0, RoundingMode.HALF_UP);
        this.remaining = remaining.setScale(0, RoundingMode.HALF_UP);
        this.used = total.subtract(remaining).setScale(0, RoundingMode.HALF_UP);
        this.usedPercent = calculatePercentage(used, total);
    }

    @Override
    public boolean isUnlimited() {
        return false;
    }

    @Override
    public Usage aggregate(Usage other) {
        if (other instanceof LimitedUsage) {
            LimitedUsage otherLimited = (LimitedUsage) other;
            return new LimitedUsage(limit.add(otherLimited.limit), remaining.add(otherLimited.remaining));
        } else if (other instanceof UnlimitedUsage) {
            return this;
        }

        throw new IllegalArgumentException("Cannot aggregate LimitedUsage with " + other.getClass().getSimpleName());
    }
}
