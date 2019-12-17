package nl.tele2.fez.aggregateusage;

import lombok.experimental.UtilityClass;
import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.bundles.AggregatedDataBundle;
import nl.tele2.fez.aggregateusage.dto.BillingPeriod;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.bundles.DataBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.ExtraCostsBundle;
import nl.tele2.fez.aggregateusage.dto.usage.LimitedUsage;
import nl.tele2.fez.aggregateusage.dto.usage.MoneyUsage;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static java.time.LocalTime.MIDNIGHT;
import static java.util.Collections.singletonList;

@UtilityClass
public class TestData {
    public static AggregateUsage createSimpleAggregateUsage() {
        return createSimpleAggregateUsage(
                new LimitedUsage(new BigDecimal(5000), new BigDecimal(1234))
        );
    }

    public static AggregateUsage createSimpleAggregateUsage(LimitedUsage currentUsage) {
        return createSimpleAggregateUsage(RepeatType.DAILY, currentUsage);
    }

    public static AggregateUsage createSimpleAggregateUsage(RepeatType repeatType, LimitedUsage currentUsage) {
        AggregateUsage usage = new AggregateUsage();

        BillingPeriod billingPeriod = new BillingPeriod();
        BillCycle billCycle = new BillCycle();
        billingPeriod.setStartDate(billCycle.startDate);
        billingPeriod.setEndDate(billCycle.endDate);
        usage.setBillingPeriod(billingPeriod);

        usage.setMsisdn("31612345678");
        usage.setLastUpdated(LocalDateTime.now());

        usage.setData(new AggregatedDataBundle("Data", "ulnl", singletonList(createUnlimitedInternetBucket(repeatType, currentUsage))));
        usage.setExtraCosts(createOutOfBundleCosts());

        return usage;
    }

    private static LocalDateTime createEndDateOfBillCycle() {
        return LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    private static LocalDateTime createStartDateOfBillCycle() {
        return LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    private static DataBundle createUnlimitedInternetBucket(RepeatType repeatType, LimitedUsage currentUsage) {
        return new DataBundle(
                "Mock Internet",
                "DRBD_R1",
                "1-23456",
                repeatType,
                LocalDateTime.now(),
                currentUsage,
                LocalDateTime.now().with(LocalTime.MIN),
                LocalDateTime.now().with(LocalTime.MAX),
                "ULNL",
                null,
                null, BundleType.DATA
        );
    }

    private static ExtraCostsBundle createOutOfBundleCosts() {
        BillCycle billCycle = new BillCycle();

        return new ExtraCostsBundle(
                "Factuur Limiet",
                "USAGE_LIMIT",
                "1-12345",
                RepeatType.BILL_CYCLE,
                LocalDateTime.now(),
                new MoneyUsage(BigDecimal.TEN, BigDecimal.ONE),
                billCycle.startDate,
                billCycle.endDate
        );
    }

    private static class BillCycle {
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;

        private BillCycle() {
            this(Clock.systemDefaultZone());
        }

        private BillCycle(Clock clock) {
            this.startDate = LocalDateTime.now(clock)
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .with(MIDNIGHT);

            this.endDate = LocalDateTime.now(clock)
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .with(LocalTime.MAX);
        }
    }
}
