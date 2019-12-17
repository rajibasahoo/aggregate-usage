package nl.tele2.fez.aggregateusage.service;

import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import io.reactivex.Single;
import nl.tele2.fez.aggregateusage.TestConstants;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import nl.tele2.fez.aggregateusage.tip.restofworld.GetAllBucketsResponse;
import nl.tele2.fez.aggregateusage.tip.restofworld.RestOfWorldBalanceResponse;
import nl.tele2.fez.common.tip.rest.TipRestCallBuilder;
import nl.tele2.fez.common.tip.rest.TipRestCallingService;
import nl.tele2.fez.common.tip.rest.TipTrackingInformation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Future;

import static nl.tele2.fez.aggregateusage.TestConstants.BPID;
import static nl.tele2.fez.aggregateusage.TestConstants.CID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestOfWorldBalanceServiceTest {
    private final static String ENDPOINT = "http://localhost:8080/restOfWorld";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private TipRestCallingService tipRestCallingService;

    private RestOfWorldBalanceService restOfWorldBalanceService;
    private TipRestCallBuilder mockTipRestCallBuilder;

    @Before
    public void setUp() {
        restOfWorldBalanceService = new RestOfWorldBalanceService(tipRestCallingService, ENDPOINT);

        mockTipRestCallBuilder = mock(TipRestCallBuilder.class);
        when(mockTipRestCallBuilder.queryParam(anyString(), anyString())).thenReturn(mockTipRestCallBuilder);
        when(tipRestCallingService.createForEndpoint(ENDPOINT, new TipTrackingInformation(BPID, CID))).thenReturn(mockTipRestCallBuilder);
    }

    @Test
    public void shouldReturnResponseWhenValid() {
        RestOfWorldBalanceResponse response = new RestOfWorldBalanceResponse();
        response.setGetAllBucketsResponse(new GetAllBucketsResponse());

        when(mockTipRestCallBuilder.get(RestOfWorldBalanceResponse.class)).thenReturn(Single.just(response));

        Future<RestOfWorldBalanceResponse> balance = restOfWorldBalanceService.getBalance(TestConstants.MSISDN, TestConstants.BPID, TestConstants.CID);
        assertThat(((AsyncResult) balance).invoke()).isEqualTo(response);
    }

    @Test
    public void shouldThrowAggregateUsageExceptionWhenIncompleteResponse() {
        exception.expect(AggregateUsageException.class);
        exception.expectMessage("Incomplete restOfWorld response for msisdn: msisdn");

        when(mockTipRestCallBuilder.get(RestOfWorldBalanceResponse.class)).thenReturn(Single.just(new RestOfWorldBalanceResponse()));

        Future<RestOfWorldBalanceResponse> balance = restOfWorldBalanceService.getBalance(TestConstants.MSISDN, TestConstants.BPID, TestConstants.CID);
        ((AsyncResult) balance).invoke();
    }

    @Test
    public void shouldReturnNullWhenNoData() {
        GetAllBucketsResponse allBucketsResponse = new GetAllBucketsResponse();
        allBucketsResponse.setCode(3002);
        allBucketsResponse.setDescription("NODATA");

        RestOfWorldBalanceResponse response = new RestOfWorldBalanceResponse();
        response.setGetAllBucketsResponse(allBucketsResponse);
        when(mockTipRestCallBuilder.get(RestOfWorldBalanceResponse.class)).thenReturn(Single.just(response));

        Future<RestOfWorldBalanceResponse> balance = restOfWorldBalanceService.getBalance(TestConstants.MSISDN, TestConstants.BPID, TestConstants.CID);
        assertNull(((AsyncResult) balance).invoke());
    }

}
