package nl.tele2.fez.aggregateusage.service;

import nl.tele2.fez.aggregateusage.dto.TopupInfo;
import nl.tele2.fez.aggregateusage.tip.national.AccountBalance;
import nl.tele2.fez.aggregateusage.tip.restofworld.DocTypeRefTnsProduct;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopupServiceTest {
    @Mock
    private FezApiAdapter fezApiAdapter;

    private TopupService topupService;

    @Before
    public void setUp() {
        topupService = new TopupService(fezApiAdapter, "http://api.example.com");


        TopupInfo topupInfo = new TopupInfo();
        topupInfo.setId("123");
        topupInfo.setDurationHours("722");
        topupInfo.setZoneId("Groen");
        topupInfo.setProductDescription("Groene Bundel (XS)");

        when(fezApiAdapter.get(anyString(), eq(TopupInfo.class), anyString()))
                .thenReturn(topupInfo);
    }

    @Test
    public void shouldRetrieveZoneForProduct() {
        DocTypeRefTnsProduct product = new DocTypeRefTnsProduct();
        product.setProductID(123L);

        String zone = topupService.getZoneForRowBundle(product);

        assertThat(zone).isEqualTo("groen");
        verify(fezApiAdapter).get(eq("http://api.example.com/topups/{productId}"), eq(TopupInfo.class), eq("123"));
    }

    @Test
    public void shouldFallbackOnHttpClientException() {
        DocTypeRefTnsProduct product = new DocTypeRefTnsProduct();
        product.setProductID(123L);

        when(fezApiAdapter.get(anyString(), eq(TopupInfo.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertEquals("unknown", topupService.getZoneForRowBundle(product));
    }


    @Test(expected = RuntimeException.class)
    public void shouldUnwrapRuntimeException() {
        DocTypeRefTnsProduct product = new DocTypeRefTnsProduct();
        product.setProductID(123L);

        when(fezApiAdapter.get(anyString(), eq(TopupInfo.class), anyString()))
                .thenThrow(new RuntimeException("hey there!"));

        topupService.getZoneForRowBundle(product);
    }

    private static class MockCheckedException extends Exception {
    }

    @Test
    public void shouldRetrieveNameForProduct() {
        final AccountBalance.BucketAllocations.Item bundle = new AccountBalance.BucketAllocations.Item();
        bundle.setBucketCategory("123");
        String name = topupService.getNameForBundle(bundle);

        assertThat(name).isEqualTo("Groene Bundel (XS)");
    }

    @Test
    public void shouldFallbackOnHttpClientExceptionForName() {
        final AccountBalance.BucketAllocations.Item bundle = new AccountBalance.BucketAllocations.Item();
        bundle.setBucketCategory("123");
        bundle.setBucketInvoiceText("callmyname");

        when(fezApiAdapter.get(anyString(), eq(TopupInfo.class), anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertEquals("callmyname", topupService.getNameForBundle(bundle));
    }

    @Test(expected = RuntimeException.class)
    public void shouldUnwrapRuntimeExceptionForName() {
        final AccountBalance.BucketAllocations.Item bundle = new AccountBalance.BucketAllocations.Item();
        bundle.setBucketCategory("123");

        when(fezApiAdapter.get(anyString(), eq(TopupInfo.class), anyString()))
                .thenThrow(new RuntimeException("ok!"));

        topupService.getNameForBundle(bundle);
    }

    @Test
    public void shouldFallbackInvoiceBucketNameForProduct() {
        final AccountBalance.BucketAllocations.Item bundle = new AccountBalance.BucketAllocations.Item();
        bundle.setBucketCategory("123");
        bundle.setBucketInvoiceText("Some Weird Bucket Name");
        String name = topupService.fallbackNameForBundle(bundle, new RuntimeException());

        assertThat(name).isEqualTo("Some Weird Bucket Name");
    }

    @Test
    public void shouldFallbackOnThresholdMessages() {
        DocTypeRefTnsProduct product = new DocTypeRefTnsProduct();
        product.setProductID(123L);
        product.getThresholdMessage().add("TH_80_1");
        product.getThresholdMessage().add("TH_100_PAARS");

        String zone = topupService.fallbackZoneForRowBundle(product, new RuntimeException());

        assertThat(zone).isEqualTo("paars");
    }

    @Test
    public void shouldReturnRoamingEu() {
        DocTypeRefTnsProduct bundle = new DocTypeRefTnsProduct();
        bundle.setName("Roaming EU");

        String zone = topupService.getZoneForRowBundle(bundle);

        assertThat(zone).isEqualTo("Roaming EU");

    }
}
