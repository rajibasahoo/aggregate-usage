package nl.tele2.fez.aggregateusage.dto.bundles;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.usage.LimitedUsage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

@Getter
public class AggregatedDataBundle {
    private static final BigDecimal TO_PERCENT = new BigDecimal(100);

    @ApiModelProperty(value = "Name of the bundle")
    private final String name;
    @ApiModelProperty(value = "Limit of the bundle")
    private final BigDecimal limit;
    @ApiModelProperty(value = "How much is used of the bundle")
    private final BigDecimal used;
    @ApiModelProperty(value = "How much is still remaining ( limit - used )")
    private final BigDecimal remaining;
    @ApiModelProperty(value = "The zone where this bundle is in", example = "nleu,ulnl,roze,groen,paars,oranje,etc")
    private final String zone;
    @ApiModelProperty(value = "List of all bundles with more details")
    private final List<DataBundle> bundles;


    public AggregatedDataBundle(String name, String zone, List<DataBundle> bundles) {
        this.name = name;
        this.bundles = bundles;
        this.limit = aggregateUsage(bundles, LimitedUsage::getLimit);
        this.used = aggregateUsage(bundles, LimitedUsage::getUsed);
        this.zone = zone;
        this.remaining = limit.subtract(used);
    }

    private BigDecimal aggregateUsage(List<DataBundle> bundles, Function<LimitedUsage, BigDecimal> usageFn) {
        return bundles.stream()
                .filter(bundle -> !"DMRU".equalsIgnoreCase(bundle.getProductName()))
                .map(DataBundle::getUsage)
                .filter(LimitedUsage.class::isInstance)
                .map(LimitedUsage.class::cast)
                .map(usageFn)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    @ApiModelProperty(value = "Percentage that is used")
    public BigDecimal getUsedPercent() {
        return used.multiply(TO_PERCENT).divide(limit, 0, RoundingMode.HALF_DOWN);
    }

    @ApiModelProperty(value = "Helper field that is true when the customer has bought a 24hour unlimited topup")
    public boolean getUnlimited24Hours() {
        return bundles.stream().anyMatch(bundle -> bundle.getType().equals(BundleType.DATA_UNLIMITED));
    }

}
