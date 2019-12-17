package nl.tele2.fez.aggregateusage.service;

import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.Unit;
import nl.tele2.fez.aggregateusage.dto.bundles.DataBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.SmsBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.VoiceBundle;
import nl.tele2.fez.aggregateusage.dto.usage.LimitedUsage;
import nl.tele2.fez.aggregateusage.dto.usage.UnlimitedUsage;
import nl.tele2.fez.aggregateusage.tip.national.AccountBalance;
import nl.tele2.fez.aggregateusage.tip.restofworld.BucketStateEnum;
import nl.tele2.fez.aggregateusage.tip.restofworld.DocTypeRefTnsBucket;
import nl.tele2.fez.aggregateusage.tip.restofworld.DocTypeRefTnsProduct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BundleFactoryTest {

    @Mock
    private TopupService topupService;
    @InjectMocks
    private BundleFactory bundleFactory;

    @Test
    public void shouldCreateVoiceSmsBundle() {
        AccountBalance.BucketAllocations.Item bucket = new AccountBalance.BucketAllocations.Item();
        bucket.setClassification("UNLIMITED_VOICE_SMS");
        bucket.setBucketRepeatType("Calendar Month");
        bucket.setBucketInvoiceText("Unlimited Bel/Sms");
        bucket.setAmountRemaining("9999999991");
        bucket.setBucketAmount("9999999999");
        bucket.setLastUsedData("2018-09-11 10:08:27.000+0200");
        bucket.setAllocationStartDate("2018-09-11 10:08:27.000+0200");
        bucket.setAllocationEndDate("2018-09-11 10:08:27.000+0200");
        bucket.setStatus("Active");

        VoiceBundle bundle = bundleFactory.createVoiceBundle(Collections.singletonList(bucket));

        assertThat(bundle.getType()).isEqualTo(BundleType.VOICE_SMS);
        assertThat(bundle.getName()).isEqualTo("Unlimited Bel/Sms");
        assertThat(bundle.getRepeatType()).isEqualTo(RepeatType.MONTHLY);
        assertThat(bundle.getUsage()).isInstanceOf(UnlimitedUsage.class);
        assertThat(bundle.getUnit()).isEqualTo(Unit.MINUTES_SMS);
        assertThat(bundle.getUsage().isUnlimited()).isTrue();

        assertThat(bundle.getLastUsage()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
        assertThat(bundle.getStartDate()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
        assertThat(bundle.getEndDate()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
    }

    @Test
    public void shouldCreateDataBundle() {
        AccountBalance.BucketAllocations.Item bucket = createBundle("DATA", "Active", "DBRM_R1");

        List<DataBundle> bundles = bundleFactory.createDataBundles(Collections.singletonList(bucket));
        DataBundle bundle = bundles.get(0);

        assertThat(bundle.getType()).isEqualTo(BundleType.DATA);
        assertThat(bundle.getName()).isEqualTo("Data");
        assertThat(bundle.getRepeatType()).isEqualTo(RepeatType.BILL_CYCLE);
        assertThat(bundle.getUsage()).isInstanceOf(LimitedUsage.class);
        LimitedUsage usage = (LimitedUsage) bundle.getUsage();
        assertThat(usage.getUsed()).isEqualTo(new BigDecimal("100"));
        assertThat(usage.getRemaining()).isEqualTo(new BigDecimal("100"));
        assertThat(usage.getLimit()).isEqualTo(new BigDecimal("200"));
        assertThat(usage.getUsedPercent()).isEqualTo(new BigDecimal("50"));
        assertThat(usage.isUnlimited()).isFalse();
        assertThat(bundle.getZone()).isEqualTo("nleu");
        assertThat(bundle.getUnit()).isEqualTo(Unit.MEGABYTES);
        assertThat(bundle.getLastUsage()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
        assertThat(bundle.getStartDate()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
        assertThat(bundle.getEndDate()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
        assertThat(bundle.getEntitlementId()).isEqualTo("I_21765");
        assertThat(bundle.getCrmRef()).isEqualTo("DMRB_R120190103144136");
        assertThat(bundle.getRecurrenceAmount()).isEqualTo(new BigDecimal(100));
        assertThat(bundle.getProductName()).isEqualTo("DBRM_R1");
    }

    @Test
    public void shouldCreateUnlimitedForADayDataBundle() {
        AccountBalance.BucketAllocations.Item bucket = createBundle("DATA", "Active", "20060");
        when(topupService.getNameForBundle(bucket)).thenReturn("Unlimited For A Day");

        List<DataBundle> bundles = bundleFactory.createDataBundles(Collections.singletonList(bucket));
        DataBundle bundle = bundles.get(0);

        assertThat(bundle.getType()).isEqualTo(BundleType.DATA_UNLIMITED);
        assertThat(bundle.getName()).isEqualTo("Unlimited For A Day");
        assertThat(bundle.getUsage()).isInstanceOf(UnlimitedUsage.class);
        UnlimitedUsage usage = (UnlimitedUsage) bundle.getUsage();
        assertThat(usage.isUnlimited()).isTrue();
        assertThat(bundle.getZone()).isEqualTo("nleu");
        assertThat(bundle.getProductName()).isEqualTo("20060");
    }

    @Test
    public void shouldCreateSmsBundle() {
        AccountBalance.BucketAllocations.Item bucket = createBundle("SMS", "Active", "21000");

        SmsBundle smsBundle = bundleFactory.createSmsBundle(Collections.singletonList(bucket));
        assertThat(smsBundle).isNotNull();
    }

    @Test
    public void shouldCreateVoiceBundle() {
        AccountBalance.BucketAllocations.Item bucket = createBundle("VOICE_SMS", "Active", "21000");

        VoiceBundle voiceBundle = bundleFactory.createVoiceBundle(Collections.singletonList(bucket));
        assertThat(voiceBundle).isNotNull();
    }

    @Test
    public void shouldCreateVoiceBundle2() {
        AccountBalance.BucketAllocations.Item bucket = createBundle("VOICE", "Active", "21000");

        VoiceBundle voiceBundle = bundleFactory.createVoiceBundle(Collections.singletonList(bucket));
        assertThat(voiceBundle).isNotNull();
    }

    @Test
    public void shouldNotCreateVoiceBundleWhenNotActive() {
        AccountBalance.BucketAllocations.Item bucket = createBundle("VOICE", "Inactive", "21000");

        VoiceBundle voiceBundle = bundleFactory.createVoiceBundle(Collections.singletonList(bucket));
        assertThat(voiceBundle).isNull();
    }

    @Test
    public void shouldCreateVoiceBundleWhenStatusNull() {
        AccountBalance.BucketAllocations.Item bucket = createBundle("VOICE", null, "21000");

        VoiceBundle voiceBundle = bundleFactory.createVoiceBundle(Collections.singletonList(bucket));
        assertThat(voiceBundle).isNotNull();
    }

    private AccountBalance.BucketAllocations.Item createBundle(String classification, String status, String bucketCategory) {
        AccountBalance.BucketAllocations.Item bucket = new AccountBalance.BucketAllocations.Item();
        bucket.setClassification(classification);
        bucket.setBucketRepeatType("Bill Cycle Month");
        bucket.setBucketInvoiceText("Data");
        bucket.setAmountRemaining("90");
        bucket.setTotalRemainingAmount("100");
        bucket.setBucketAmount("200");
        bucket.setLastUsedData("2018-09-11 10:08:27.000+0200");
        bucket.setAllocationStartDate("2018-09-11 10:08:27.000+0200");
        bucket.setAllocationEndDate("2018-09-11 10:08:27.000+0200");
        bucket.setBucketEntitlementId("I_21765");
        bucket.setParentCrmRef("DMRB_R120190103144136");
        bucket.setRecurrenceAmount("100");
        bucket.setBucketCategory(bucketCategory);
        bucket.setStatus(status);
        return bucket;
    }

    @Test
    public void shouldCreateUnlimitedDataBundle() {
        AccountBalance.UsageLimits.Item limit = new AccountBalance.UsageLimits.Item();
        limit.setClassification("DATA|UL");
        limit.setRepeatType("Bill Cycle");
        limit.setInvoiceText("Unlimited Data");
        limit.setUnitOfMeasure("Bytes");
        limit.setCurrentBalance("100000000");
        limit.setTresholdValue("200000000");
        limit.setOffset("200000000");
        limit.setLastUsedDate("2018-09-11 10:08:27.000+0200");
        limit.setCounterStartDate("2018-09-11 10:08:27.000+0200");
        limit.setCounterEndDate("2018-09-11 10:08:27.000+0200");
        limit.setStatus(null);

        List<DataBundle> bundles = bundleFactory.createUnlimitedBundles(Collections.singletonList(limit));
        DataBundle bundle = bundles.get(0);

        assertThat(bundle.getType()).isEqualTo(BundleType.DATA);
        assertThat(bundle.getName()).isEqualTo("Unlimited Data");
        assertThat(bundle.getRepeatType()).isEqualTo(RepeatType.BILL_CYCLE);
        assertThat(bundle.getUsage()).isInstanceOf(LimitedUsage.class);
        LimitedUsage usage = (LimitedUsage) bundle.getUsage();
        assertThat(usage.getUsed()).isEqualTo(new BigDecimal("100"));
        assertThat(usage.getRemaining()).isEqualTo(new BigDecimal("300"));
        assertThat(usage.getLimit()).isEqualTo(new BigDecimal("400"));
        assertThat(usage.getUsedPercent()).isEqualTo(new BigDecimal("25"));
        assertThat(usage.isUnlimited()).isTrue();
        assertThat(bundle.getZone()).isEqualTo("ulnl");
        assertThat(bundle.getUnit()).isEqualTo(Unit.MEGABYTES);

        assertThat(bundle.getLastUsage()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
        assertThat(bundle.getStartDate()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
        assertThat(bundle.getEndDate()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
    }

    @Test
    public void shouldCreateTopupBundle() {
        DocTypeRefTnsBucket bucket = new DocTypeRefTnsBucket();
        DocTypeRefTnsProduct product = new DocTypeRefTnsProduct();
        product.setProductID(20038L);
        product.setName("Oranje Bundel 350 business");
        product.setCapacity(153600L);
        product.setCapacityUnit(1);
        product.getThresholdMessage().add("TH_100_PAARS");
        bucket.setProduct(product);
        bucket.setCommittedVolume(10240L);
        bucket.setStartDate(stringToXMLGregorianCalendar("2018-09-11T10:08:27.000+02:00"));
        bucket.setEndDate(stringToXMLGregorianCalendar("2018-09-21T10:08:27.000+02:00"));
        bucket.setBucketState(BucketStateEnum.ACTIVE);

        when(topupService.getZoneForRowBundle(product)).thenReturn("oranje");

        List<DataBundle> bundles = bundleFactory.createRoamingBundles(Collections.singletonList(bucket));
        DataBundle bundle = bundles.get(0);

        assertThat(bundle.getType()).isEqualTo(BundleType.DATA);
        assertThat(bundle.getName()).isEqualTo("Oranje Bundel 350 business");
        assertThat(bundle.getUsage()).isInstanceOf(LimitedUsage.class);
        LimitedUsage usage = (LimitedUsage) bundle.getUsage();
        assertThat(usage.getUsed()).isEqualTo(new BigDecimal("10"));
        assertThat(usage.getRemaining()).isEqualTo(new BigDecimal("140"));
        assertThat(usage.getLimit()).isEqualTo(new BigDecimal("150"));
        assertThat(usage.getUsedPercent()).isEqualTo(new BigDecimal("7"));
        assertThat(usage.isUnlimited()).isFalse();
        assertThat(bundle.getZone()).isEqualTo("oranje");
        assertThat(bundle.getUnit()).isEqualTo(Unit.MEGABYTES);

        assertThat(bundle.getStartDate()).isEqualTo(LocalDateTime.parse("2018-09-11T10:08:27"));
        assertThat(bundle.getEndDate()).isEqualTo(LocalDateTime.parse("2018-09-21T10:08:27"));
    }

    @Test
    public void shouldMergeLimits() {
        AccountBalance.BucketAllocations.Item limit = new AccountBalance.BucketAllocations.Item();
        limit.setClassification("FUP|PR");
        limit.setStatus("Active");
        limit.setTotalRemainingAmount("200");
        limit.setBucketAmount("300");
        limit.setBucketRepeatType("Bill Cycle Month");
        limit.setAllocationStartDate("2019-05-01 00:00:00.000+0200");
        limit.setAllocationEndDate("2019-05-31 23:59:59.000+0200");
        limit.setLastUsedData("2019-05-01 00:00:00.000+0200");

        AccountBalance.BucketAllocations.Item limit2 = new AccountBalance.BucketAllocations.Item();
        limit2.setClassification("FUP|PR");
        limit2.setStatus("Active");
        limit2.setTotalRemainingAmount("500");
        limit2.setBucketAmount("600");
        limit2.setBucketRepeatType("Bill Cycle Month");
        limit2.setAllocationStartDate("2019-05-20 00:00:00.000+0200");
        limit2.setAllocationEndDate("2019-05-20 23:59:59.000+0200");
        limit2.setLastUsedData("2019-05-20 01:00:00.000+0200");

        DataBundle dataLimitEu = bundleFactory.createDataLimitEu(Arrays.asList(limit, limit2));
        assertThat(((LimitedUsage) dataLimitEu.getUsage()).getLimit()).isEqualTo("900");
        assertThat(((LimitedUsage) dataLimitEu.getUsage()).getRemaining()).isEqualTo("700");
        assertThat(dataLimitEu.getStartDate().toLocalDate()).isEqualTo(LocalDate.of(2019, 5, 1));
        assertThat(dataLimitEu.getEndDate().toLocalDate()).isEqualTo(LocalDate.of(2019, 5, 31));
    }

    private XMLGregorianCalendar stringToXMLGregorianCalendar(String date) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }

}
