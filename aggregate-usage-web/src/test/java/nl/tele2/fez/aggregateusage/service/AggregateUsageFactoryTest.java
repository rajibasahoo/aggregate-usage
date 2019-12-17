package nl.tele2.fez.aggregateusage.service;

import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.bundles.DataBundle;
import nl.tele2.fez.aggregateusage.dto.usage.LimitedUsage;
import nl.tele2.fez.aggregateusage.tip.national.AccountBalance;
import nl.tele2.fez.aggregateusage.tip.national.Body;
import nl.tele2.fez.aggregateusage.tip.national.GetBalanceOutput;
import nl.tele2.fez.aggregateusage.tip.national.NationalBalanceResponse;
import nl.tele2.fez.aggregateusage.tip.restofworld.DocTypeRefTnsBucket;
import nl.tele2.fez.aggregateusage.tip.restofworld.GetAllBucketsResponse;
import nl.tele2.fez.aggregateusage.tip.restofworld.RestOfWorldBalanceResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AggregateUsageFactoryTest {

    @Mock
    private BundleFactory bundleFactory;

    @InjectMocks
    private AggregateUsageFactory aggregateUsageFactory;

    @Test
    public void shouldSetFields() {
        Pair<NationalBalanceResponse, RestOfWorldBalanceResponse> bundles = createBundles();

        AggregateUsage aggregateUsage = aggregateUsageFactory.createAggregateUsage(bundles.getKey(), bundles.getValue());

        assertThat(aggregateUsage.getBillingPeriod().getStartDate()).isEqualTo(LocalDateTime.parse("2018-12-01T00:00:00"));
        assertThat(aggregateUsage.getBillingPeriod().getEndDate()).isEqualTo(LocalDateTime.parse("2018-12-31T23:59:59"));

        assertThat(aggregateUsage.getUnbilledUsage().getAmount()).isEqualTo("126.00");
        assertThat(aggregateUsage.getUnbilledUsage().getUnit()).isEqualTo("EUR");

        assertThat(aggregateUsage.isUnlimitedData()).isFalse();
        assertThat(aggregateUsage.isUnlimitedVoice()).isFalse();

        verify(bundleFactory).createVoiceBundle(bundles.getLeft().getGetBalanceOutput().getBody().getACCOUNTBALANCE().getBucketAllocations().getItem());
        verify(bundleFactory).createSmsBundle(bundles.getLeft().getGetBalanceOutput().getBody().getACCOUNTBALANCE().getBucketAllocations().getItem());
        verify(bundleFactory).createDataBundles(bundles.getLeft().getGetBalanceOutput().getBody().getACCOUNTBALANCE().getBucketAllocations().getItem());
        verify(bundleFactory).createDataLimitEu(bundles.getLeft().getGetBalanceOutput().getBody().getACCOUNTBALANCE().getBucketAllocations().getItem());

        verify(bundleFactory).createExtraCosts(bundles.getLeft().getGetBalanceOutput().getBody().getACCOUNTBALANCE().getUsageLimits().getItem());
        verify(bundleFactory).createUnlimitedBundles(bundles.getLeft().getGetBalanceOutput().getBody().getACCOUNTBALANCE().getUsageLimits().getItem());
    }

    @Test
    public void shouldSetBillingPeriodWithOtherTimezone() {
        Pair<NationalBalanceResponse, RestOfWorldBalanceResponse> bundles = createBundles();

        bundles.getKey().getGetBalanceOutput().getBody().getACCOUNTBALANCE().setBillingPeriodStartDate("2018-12-01 00:00:00.000+0000");

        AggregateUsage aggregateUsage = aggregateUsageFactory.createAggregateUsage(bundles.getKey(), bundles.getValue());

        assertThat(aggregateUsage.getBillingPeriod().getStartDate()).isEqualTo(LocalDateTime.parse("2018-12-01T01:00:00"));
    }

    @Test
    public void shouldGetNationalBalance() {
        Pair<NationalBalanceResponse, RestOfWorldBalanceResponse> bundles = createBundles();
        AggregateUsage aggregateUsage = aggregateUsageFactory.createNationalUsage(bundles.getKey());

        assertThat(aggregateUsage.getBillingPeriod().getStartDate()).isEqualTo(LocalDateTime.parse("2018-12-01T00:00:00"));
        assertThat(aggregateUsage.getBillingPeriod().getEndDate()).isEqualTo(LocalDateTime.parse("2018-12-31T23:59:59"));

        assertThat(aggregateUsage.isUnlimitedData()).isFalse();
        assertThat(aggregateUsage.isUnlimitedVoice()).isFalse();
    }

    private Pair<NationalBalanceResponse, RestOfWorldBalanceResponse> createBundles() {
        AccountBalance accountbalance = new AccountBalance();
        accountbalance.setBucketAllocations(new AccountBalance.BucketAllocations());

        AccountBalance.UsageLimits usageLimits = new AccountBalance.UsageLimits();

        List<AccountBalance.UsageLimits.Item> limits = asList(
                new AccountBalance.UsageLimits.Item(),
                new AccountBalance.UsageLimits.Item(),
                new AccountBalance.UsageLimits.Item()
        );

        usageLimits.getItem().addAll(limits);
        accountbalance.setUsageLimits(usageLimits);

        // set unbilled amount
        accountbalance.setCurrency("EUR");
        accountbalance.setUnbilledAmount("12600");

        // set billing period
        accountbalance.setBillingPeriodStartDate("2018-12-01 00:00:00.000+0100");
        accountbalance.setBillingPeriodEndDate("2018-12-31 23:59:59.000+0100");

        Body body = new Body();
        body.setACCOUNTBALANCE(accountbalance);

        GetBalanceOutput getBalanceOutput = new GetBalanceOutput();
        getBalanceOutput.setBody(body);

        NationalBalanceResponse nationalBalance = new NationalBalanceResponse();
        nationalBalance.setGetBalanceOutput(getBalanceOutput);

        RestOfWorldBalanceResponse restOfWorldBalance = new RestOfWorldBalanceResponse();
        GetAllBucketsResponse allBucketsResponse = new GetAllBucketsResponse();
        DocTypeRefTnsBucket rowBucket = new DocTypeRefTnsBucket();
        allBucketsResponse.getBuckets().add(rowBucket);

        DataBundle bundle1 = new DataBundle(
                "Roaming EU",
                "DRBD_R1",
                "1-23456",
                RepeatType.BILL_CYCLE,
                LocalDateTime.now(),
                new LimitedUsage(BigDecimal.ONE, BigDecimal.valueOf(200)),
                LocalDateTime.now().with(LocalTime.MIN),
                LocalDateTime.now().with(LocalTime.MAX),
                "NLEU",
                null,
                null,
                BundleType.DATA
        );

        DataBundle bundle2 = new DataBundle(
                "Paarse Bundel (klein)",
                "DRBD_R1",
                "1-23456",
                RepeatType.BILL_CYCLE,
                LocalDateTime.now(),
                new LimitedUsage(BigDecimal.ONE, BigDecimal.valueOf(200)),
                LocalDateTime.now().with(LocalTime.MIN),
                LocalDateTime.now().with(LocalTime.MAX),
                "ROW",
                null,
                null,
                BundleType.DATA
        );

        when(bundleFactory.createRoamingBundles(allBucketsResponse.getBuckets())).thenReturn(Arrays.asList(bundle1, bundle2));
        restOfWorldBalance.setGetAllBucketsResponse(allBucketsResponse);

        return Pair.of(nationalBalance, restOfWorldBalance);
    }
}