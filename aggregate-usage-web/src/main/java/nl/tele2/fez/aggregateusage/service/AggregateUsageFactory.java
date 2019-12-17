package nl.tele2.fez.aggregateusage.service;

import lombok.AllArgsConstructor;
import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.dto.BillingPeriod;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.UnbilledUsage;
import nl.tele2.fez.aggregateusage.dto.bundles.AggregatedDataBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.DataBundle;
import nl.tele2.fez.aggregateusage.tip.national.AccountBalance;
import nl.tele2.fez.aggregateusage.tip.national.NationalBalanceResponse;
import nl.tele2.fez.aggregateusage.tip.restofworld.RestOfWorldBalanceResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.tele2.fez.aggregateusage.util.NumberUtil.centsToEuro;
import static nl.tele2.fez.aggregateusage.service.BundleFactory.REGULAR_SUBSCRIPTION_BUNDLE;
import static nl.tele2.fez.aggregateusage.service.BundleFactory.UNLIMITED_NATIONAL_BUNDLE;
import static nl.tele2.fez.aggregateusage.service.DateConverters.parseAndLocalize;

@Component
@AllArgsConstructor
public class AggregateUsageFactory {

    private static final String BUNDLE = "Bundel";
    private static final String SPACE = " ";
    private static final String UNKNOWN_ZONE = "Onbekende zone";

    private static final Comparator<DataBundle> DATA_BUNDLE_TYPE_COMPARATOR = Comparator.comparingInt(bundle -> BundleType.DATA_UNLIMITED == bundle.getType() ? 0 : 1);

    private final BundleFactory bundleFactory;

    public AggregateUsage createAggregateUsage(NationalBalanceResponse nationalBalance, RestOfWorldBalanceResponse restOfWorldBalance) {
        AggregateUsage aggregateUsage = createNationalUsage(nationalBalance);
        addRestOfWorldBalance(aggregateUsage, restOfWorldBalance);
        return aggregateUsage;
    }

    public AggregateUsage createNationalUsage(NationalBalanceResponse nationalBalance) {
        AggregateUsage aggregateUsage = new AggregateUsage();
        addNationalBalance(aggregateUsage, nationalBalance);
        return aggregateUsage;
    }

    private void addNationalBalance(AggregateUsage aggregateUsage, NationalBalanceResponse nationalBalance) {
        AccountBalance accountbalance = nationalBalance.getGetBalanceOutput().getBody().getACCOUNTBALANCE();
        if (accountbalance != null) {
            BillingPeriod billingPeriod = new BillingPeriod();
            billingPeriod.setStartDate(parseAndLocalize(accountbalance.getBillingPeriodStartDate()));
            billingPeriod.setEndDate(parseAndLocalize(accountbalance.getBillingPeriodEndDate()));
            aggregateUsage.setBillingPeriod(billingPeriod);

            UnbilledUsage unbilledUsage = new UnbilledUsage(
                    centsToEuro(new BigDecimal(accountbalance.getUnbilledAmount())).toString(),
                    accountbalance.getCurrency()
            );
            aggregateUsage.setUnbilledUsage(unbilledUsage);

            List<AccountBalance.BucketAllocations.Item> buckets = accountbalance.getBucketAllocations().getItem();
            List<AccountBalance.UsageLimits.Item> limits = accountbalance.getUsageLimits().getItem();

            List<DataBundle> nationalDataBundles = Stream.concat(
                    bundleFactory.createDataBundles(buckets).stream(),
                    bundleFactory.createUnlimitedBundles(limits).stream())
                    .filter(Objects::nonNull)
                    .sorted(DATA_BUNDLE_TYPE_COMPARATOR)
                    .collect(Collectors.toList());

            String zone = determineZoneForNationalBundles(nationalDataBundles);
            AggregatedDataBundle nationalBundles = nationalDataBundles.isEmpty() ? null : new AggregatedDataBundle("Data", zone, nationalDataBundles);

            aggregateUsage.setData(nationalBundles);

            aggregateUsage.setDataLimitEu(bundleFactory.createDataLimitEu(buckets));

            aggregateUsage.setVoiceSms(bundleFactory.createVoiceBundle(buckets));

            aggregateUsage.setExtraCosts(bundleFactory.createExtraCosts(limits));

            aggregateUsage.setSms(bundleFactory.createSmsBundle(buckets));
        }
    }

    private void addRestOfWorldBalance(AggregateUsage aggregateUsage, RestOfWorldBalanceResponse restOfWorldBalance) {
        if (restOfWorldBalance != null) {
            List<AggregatedDataBundle> aggregatedRoamingDataBundles = bundleFactory.createRoamingBundles(restOfWorldBalance.getGetAllBucketsResponse().getBuckets()).stream()
                    .collect(Collectors.groupingBy(DataBundle::getZone))
                    .entrySet()
                    .stream()
                    .map(entry -> new AggregatedDataBundle(createName(entry.getValue()), entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            aggregateUsage.setRoamingBundles(aggregatedRoamingDataBundles);
        }
    }

    private String createName(List<DataBundle> bundles) {
        return bundles.stream().map(DataBundle::getName).findFirst().map(this::extractName).orElse(UNKNOWN_ZONE);
    }

    private String extractName(String name) {
        if (name.contains(BUNDLE)) {
            int totalLength = name.indexOf(SPACE) + SPACE.length() + BUNDLE.length();
            return name.substring(0, totalLength);
        }

        return name;
    }

    private String determineZoneForNationalBundles(List<DataBundle> bundles) {
        return bundles.stream().anyMatch(bundle -> bundle.getUsage().isUnlimited()) ? UNLIMITED_NATIONAL_BUNDLE : REGULAR_SUBSCRIPTION_BUNDLE;
    }
}
