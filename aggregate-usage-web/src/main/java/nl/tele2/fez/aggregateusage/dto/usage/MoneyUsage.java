package nl.tele2.fez.aggregateusage.dto.usage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static nl.tele2.fez.aggregateusage.util.NumberUtil.calculatePercentage;

@Getter
public class MoneyUsage implements Usage {

    @ApiModelProperty(value = "The spending limit for this customer", example = "250.00")
    private final String limit;
    @ApiModelProperty(value = "How much the customer has used", example = "30.00")
    private final String used;
    @ApiModelProperty(value = "The amount that is remaining", example = "220")
    private final String remaining;
    @ApiModelProperty(value = "Percentage that is used")
    private final BigDecimal usedPercent;

    public MoneyUsage(BigDecimal total, BigDecimal remaining) {
        this.limit = total.setScale(2, RoundingMode.HALF_UP).toString();
        this.remaining = remaining.setScale(2, RoundingMode.HALF_UP).toString();
        this.used = total.subtract(remaining).setScale(2, RoundingMode.HALF_UP).toString();
        this.usedPercent = calculatePercentage(new BigDecimal(used), total);
    }

    @Override
    public boolean isUnlimited() {
        return false;
    }

    @Override
    public Usage aggregate(Usage other) {
        if (other instanceof MoneyUsage) {
            MoneyUsage otherMoney = (MoneyUsage) other;
            return new MoneyUsage(
                    new BigDecimal(limit).add(new BigDecimal(otherMoney.limit)),
                    new BigDecimal(remaining).add(new BigDecimal(otherMoney.remaining))
            );
        }
        throw new IllegalArgumentException("Cannot aggregate MoneyUsage with " + other.getClass().getSimpleName());
    }
}
