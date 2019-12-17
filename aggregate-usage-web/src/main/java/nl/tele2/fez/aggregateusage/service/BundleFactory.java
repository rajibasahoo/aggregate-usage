package nl.tele2.fez.aggregateusage.service;

import lombok.AllArgsConstructor;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.Unit;
import nl.tele2.fez.aggregateusage.dto.UnitConverter;
import nl.tele2.fez.aggregateusage.dto.bundles.DataBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.ExtraCostsBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.SmsBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.VoiceBundle;
import nl.tele2.fez.aggregateusage.dto.usage.FairUsePolicyUsage;
import nl.tele2.fez.aggregateusage.dto.usage.LimitedUsage;
import nl.tele2.fez.aggregateusage.dto.usage.MoneyUsage;
import nl.tele2.fez.aggregateusage.dto.usage.UnlimitedUsage;
import nl.tele2.fez.aggregateusage.dto.usage.Usage;
import nl.tele2.fez.aggregateusage.tip.national.AccountBalance;
import nl.tele2.fez.aggregateusage.tip.restofworld.BucketStateEnum;
import nl.tele2.fez.aggregateusage.tip.restofworld.DocTypeRefTnsBucket;
import nl.tele2.fez.aggregateusage.tip.restofworld.DocTypeRefTnsProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static nl.tele2.fez.aggregateusage.service.DateConverters.parseAndLocalize;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Component
@AllArgsConstructor
public class BundleFactory {
    private static final BigDecimal BYTE_TO_MEGABYTES = new BigDecimal(1_000_000);

    static final String UNLIMITED_NATIONAL_BUNDLE = "ulnl";
    static final String REGULAR_SUBSCRIPTION_BUNDLE = "nleu";
    private static final String UNLIMITED_FOR_A_DAY_ID = "20060";

    private static final Predicate<AccountBalance.BucketAllocations.Item> IS_DATA = it -> hasClassification(it.getClassification(), "DATA", "DATA|PR");
    private static final Predicate<AccountBalance.BucketAllocations.Item> IS_VOICE = it -> hasClassification(it.getClassification(), "UNLIMITED_VOICE_SMS", "VOICE_SMS|UL", "VOICE_SMS", "VOICE");
    private static final Predicate<AccountBalance.BucketAllocations.Item> IS_DATA_LIMIT_EU = it -> hasClassification(it.getClassification(), "FUP|PR");
    private static final Predicate<AccountBalance.BucketAllocations.Item> IS_SMS_BUNDLE = it -> hasClassification(it.getClassification(), "UNLIMITED_SMS", "SMS");
    private static final Predicate<AccountBalance.BucketAllocations.Item> IS_ACTIVE_BUCKET = it -> it.getStatus() == null || "Active".equalsIgnoreCase(it.getStatus());
    private static final Predicate<AccountBalance.UsageLimits.Item> IS_ACTIVE_LIMIT = it -> it.getStatus() == null || "Active".equalsIgnoreCase(it.getStatus());
    private static final Predicate<AccountBalance.UsageLimits.Item> IS_EXTRA_COSTS = it -> hasClassification(it.getClassification(), "CON_USAGE_LIMIT");
    private static final Predicate<AccountBalance.UsageLimits.Item> IS_UNLIMITED_BUNDLE = it -> hasClassification(it.getClassification(), "DATA|UL|LOC", "DATA|UL");

    private final TopupService topupService;

    private static boolean hasClassification(String classification, String... values) {
        return Arrays.asList(values).contains(classification);
    }

    public List<DataBundle> createDataBundles(List<AccountBalance.BucketAllocations.Item> buckets) {
        return buckets.stream()
                .filter(IS_DATA)
                .filter(IS_ACTIVE_BUCKET)
                .map(bucket -> {
                    BigDecimal recurrenceAmount = bucket.getRecurrenceAmount() == null ? null : new BigDecimal(bucket.getRecurrenceAmount());
                    Usage usage = getUsageForBucket(bucket);
                    BundleType type = getTypeForBucket(bucket);
                    String productTitle = getTitleForBucket(bucket);

                    return createBundle(productTitle, bucket, REGULAR_SUBSCRIPTION_BUNDLE, bucket.getBucketEntitlementId(), recurrenceAmount, usage, type);
                })
                .collect(Collectors.toList());
    }

    private String getTitleForBucket(AccountBalance.BucketAllocations.Item bucket) {
        if (isNumeric(bucket.getBucketCategory())) {
            return topupService.getNameForBundle(bucket);
        }

        return bucket.getBucketInvoiceText();
    }

    private BundleType getTypeForBucket(AccountBalance.BucketAllocations.Item bucket) {
        if (UNLIMITED_FOR_A_DAY_ID.equals(bucket.getBucketCategory())) {
            return BundleType.DATA_UNLIMITED;
        }

        return BundleType.DATA;
    }

    private Usage getUsageForBucket(AccountBalance.BucketAllocations.Item bucket) {
        if (UNLIMITED_FOR_A_DAY_ID.equals(bucket.getBucketCategory())) {
            return new UnlimitedUsage();
        }

        return new LimitedUsage(new BigDecimal(bucket.getBucketAmount()), new BigDecimal(bucket.getTotalRemainingAmount()));
    }

    public VoiceBundle createVoiceBundle(List<AccountBalance.BucketAllocations.Item> buckets) {
        return buckets.stream()
                .filter(IS_VOICE)
                .filter(IS_ACTIVE_BUCKET)
                .map(bucket -> {
                    switch (bucket.getClassification()) {
                        case "UNLIMITED_VOICE_SMS":
                        case "VOICE_SMS|UL":
                            return createVoiceBundle(bucket, new UnlimitedUsage(), BundleType.VOICE_SMS, Unit.MINUTES_SMS);
                        case "VOICE_SMS":
                            return createVoiceBundle(
                                    bucket,
                                    new LimitedUsage(new BigDecimal(bucket.getBucketAmount()), new BigDecimal(bucket.getTotalRemainingAmount())),
                                    BundleType.VOICE_SMS,
                                    Unit.MINUTES_SMS);
                        case "VOICE":
                            return createVoiceBundle(
                                    bucket,
                                    new LimitedUsage(new BigDecimal(bucket.getBucketAmount()), new BigDecimal(bucket.getTotalRemainingAmount())),
                                    BundleType.VOICE,
                                    Unit.MINUTES);
                        default:
                            return null;
                    }
                })
                .findFirst()
                .orElse(null);

    }

    private VoiceBundle createVoiceBundle(AccountBalance.BucketAllocations.Item bucket, Usage usage, BundleType bundleType, Unit unit) {
        return new VoiceBundle(
                bucket.getBucketInvoiceText(),
                defaultIfBlank(bucket.getProductName(), bucket.getBucketCategory()),
                defaultIfBlank(bucket.getParentCrmRef(), bucket.getCrmRef()),
                RepeatType.parse(bucket.getBucketRepeatType()),
                parseAndLocalize(bucket.getLastUsedData()),
                usage,
                parseAndLocalize(bucket.getAllocationStartDate()),
                parseAndLocalize(bucket.getAllocationEndDate()),
                bundleType,
                unit
        );
    }

    public List<DataBundle> createUnlimitedBundles(List<AccountBalance.UsageLimits.Item> limits) {
        return limits.stream()
                .filter(IS_UNLIMITED_BUNDLE)
                .filter(IS_ACTIVE_LIMIT)
                .map(limit -> {
                    BigDecimal used = new BigDecimal(limit.getCurrentBalance());
                    BigDecimal offset = limit.getOffset() != null ? new BigDecimal(limit.getOffset()) : BigDecimal.ZERO;
                    BigDecimal total = new BigDecimal(limit.getTresholdValue()).add(offset);
                    String unit = limit.getUnitOfMeasure();
                    if ("Bytes".equals(unit)) {
                        total = total.divide(BYTE_TO_MEGABYTES, RoundingMode.HALF_UP);
                        used = used.divide(BYTE_TO_MEGABYTES, RoundingMode.HALF_UP);
                    }
                    return createDataBundle(limit, used, total);
                })
                .collect(Collectors.toList());


    }

    private DataBundle createDataBundle(AccountBalance.UsageLimits.Item limit, BigDecimal used, BigDecimal total) {
        return new DataBundle(
                limit.getInvoiceText(),
                limit.getProductName(),
                limit.getCrmRef(),
                RepeatType.parse(limit.getRepeatType()),
                parseAndLocalize(limit.getLastUsedDate()),
                new FairUsePolicyUsage(total, total.subtract(used)),
                parseAndLocalize(limit.getCounterStartDate()),
                parseAndLocalize(limit.getCounterEndDate()),
                UNLIMITED_NATIONAL_BUNDLE,
                null,
                null,
                BundleType.DATA
        );
    }

    public SmsBundle createSmsBundle(List<AccountBalance.BucketAllocations.Item> buckets) {
        return buckets.stream()
                .filter(IS_SMS_BUNDLE)
                .filter(IS_ACTIVE_BUCKET)
                .map(bucket -> new SmsBundle(
                        bucket.getBucketInvoiceText(),
                        defaultIfBlank(bucket.getProductName(), bucket.getBucketCategory()),
                        defaultIfBlank(bucket.getParentCrmRef(), bucket.getCrmRef()),
                        RepeatType.parse(bucket.getBucketRepeatType()),
                        parseAndLocalize(bucket.getLastUsedData()),
                        new LimitedUsage(new BigDecimal(bucket.getBucketAmount()), new BigDecimal(bucket.getTotalRemainingAmount())),
                        parseAndLocalize(bucket.getAllocationStartDate()),
                        parseAndLocalize(bucket.getAllocationEndDate())
                )).findFirst()
                .orElse(null);

    }

    public ExtraCostsBundle createExtraCosts(List<AccountBalance.UsageLimits.Item> limits) {
        return limits.stream()
                .filter(IS_EXTRA_COSTS)
                .map(limit -> {
                    String type = limit.getClassification();

                    if (!"CON_USAGE_LIMIT".equals(type)) {
                        return null;
                    }

                    RepeatType repeatType = RepeatType.parse(limit.getRepeatType());
                    String name = limit.getInvoiceText();
                    String productName = limit.getProductName();
                    String crmRef = limit.getCrmRef();
                    LocalDateTime lastUsage = parseAndLocalize(limit.getLastUsedDate());

                    LocalDateTime startDate = parseAndLocalize(limit.getCounterStartDate());
                    LocalDateTime endDate = parseAndLocalize(limit.getCounterEndDate());

                    BigDecimal used = new BigDecimal(limit.getCurrentBalance());
                    BigDecimal total = new BigDecimal(limit.getTresholdValue());

                    return new ExtraCostsBundle(name, productName, crmRef, repeatType, lastUsage, new MoneyUsage(total, total.subtract(used)), startDate, endDate);
                })
                .findFirst()
                .orElse(null);
    }

    public DataBundle createDataLimitEu(List<AccountBalance.BucketAllocations.Item> buckets) {
        return buckets.stream()
                .filter(IS_DATA_LIMIT_EU)
                .filter(IS_ACTIVE_BUCKET)
                .map(bucket -> createBundle(bucket, "roaming_eu", null, null, new LimitedUsage(new BigDecimal(bucket.getBucketAmount()), new BigDecimal(bucket.getTotalRemainingAmount())), BundleType.DATA))
                .reduce(this::mergeDataLimits)
                .orElse(null);


    }

    private DataBundle mergeDataLimits(DataBundle dataBundle, DataBundle dataBundle2) {
        return new DataBundle(
                dataBundle.getName(),
                dataBundle.getProductName(),
                dataBundle.getCrmRef(),
                dataBundle.getRepeatType(),
                dataBundle.getLastUsage().isAfter(dataBundle2.getStartDate()) ? dataBundle.getLastUsage() : dataBundle2.getLastUsage(),
                dataBundle.getUsage().aggregate(dataBundle2.getUsage()),
                dataBundle.getStartDate().isBefore(dataBundle2.getStartDate()) ? dataBundle.getStartDate() : dataBundle2.getStartDate(),
                dataBundle.getEndDate().isAfter(dataBundle2.getEndDate()) ? dataBundle.getEndDate() : dataBundle2.getEndDate(),
                dataBundle.getZone(),
                dataBundle.getEntitlementId(),
                dataBundle.getRecurrenceAmount(),
                dataBundle.getType()
        );
    }

    public List<DataBundle> createRoamingBundles(List<DocTypeRefTnsBucket> buckets) {
        return buckets.stream()
                .filter(it -> it.getBucketState() == BucketStateEnum.ACTIVE)
                .map(bucket -> {
                    DocTypeRefTnsProduct product = bucket.getProduct();

                    BigDecimal total = UnitConverter.convertToMegaBytesBinary(product.getCapacity(), product.getCapacityUnit());
                    BigDecimal used = UnitConverter.convertToMegaBytesBinary(bucket.getCommittedVolume(), product.getCapacityUnit());

                    String zone = topupService.getZoneForRowBundle(product);
                    return new DataBundle(
                            product.getName(),
                            product.getProductID().toString(),
                            null,
                            RepeatType.ONE_TIME,
                            null,
                            new LimitedUsage(total, total.subtract(used)),
                            parseAndLocalize(bucket.getStartDate()),
                            parseAndLocalize(bucket.getEndDate()),
                            zone,
                            null,
                            null,
                            BundleType.DATA
                    );
                })
                .collect(Collectors.toList());
    }

    private DataBundle createBundle(AccountBalance.BucketAllocations.Item bucket, String zone, String entitlementId, BigDecimal recurrenceAmount, Usage usage, BundleType type) {
        return createBundle(
                bucket.getBucketInvoiceText(),
                bucket,
                zone,
                entitlementId,
                recurrenceAmount,
                usage,
                type
        );
    }

    private DataBundle createBundle(String productTitle, AccountBalance.BucketAllocations.Item bucket, String zone, String entitlementId, BigDecimal recurrenceAmount, Usage usage, BundleType type) {
        return new DataBundle(
                productTitle,
                defaultIfBlank(bucket.getProductName(), bucket.getBucketCategory()),
                defaultIfBlank(bucket.getParentCrmRef(), bucket.getCrmRef()),
                RepeatType.parse(bucket.getBucketRepeatType()),
                parseAndLocalize(bucket.getLastUsedData()),
                usage,
                parseAndLocalize(bucket.getAllocationStartDate()),
                parseAndLocalize(bucket.getAllocationEndDate()),
                zone,
                entitlementId,
                recurrenceAmount,
                type
        );
    }
}
