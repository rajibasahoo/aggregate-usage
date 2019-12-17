package nl.tele2.fez.aggregateusage.service;

import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import io.reactivex.Single;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import nl.tele2.fez.aggregateusage.tip.national.AccountBalance;
import nl.tele2.fez.aggregateusage.tip.national.Body;
import nl.tele2.fez.aggregateusage.tip.national.GetBalanceOutput;
import nl.tele2.fez.aggregateusage.tip.national.NationalBalanceResponse;
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
import static nl.tele2.fez.aggregateusage.TestConstants.MSISDN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NationalBalanceServiceTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private TipRestCallingService tipRestCallingService;

    private NationalBalanceService nationalBalanceService;
    private TipRestCallBuilder mockTipRestCallBuilder;

    @Before
    public void setUp() {
        String endpoint = "http://localhost:8080";
        nationalBalanceService = new NationalBalanceService(tipRestCallingService, endpoint);

        mockTipRestCallBuilder = mock(TipRestCallBuilder.class);
        when(mockTipRestCallBuilder.queryParam(anyString(), anyString())).thenReturn(mockTipRestCallBuilder);

        when(tipRestCallingService.createForEndpoint(endpoint, new TipTrackingInformation(BPID, CID))).thenReturn(mockTipRestCallBuilder);
    }

    @Test
    public void shouldGetBalance() {
        NationalBalanceResponse NationalBalanceResponse = createNationalBalanceResponse(null, null);
        when(mockTipRestCallBuilder.get(NationalBalanceResponse.class)).thenReturn(Single.just(NationalBalanceResponse));


        Future<NationalBalanceResponse> balance = nationalBalanceService.getBalance(MSISDN, BPID, CID);
        assertThat(((AsyncResult) balance).invoke()).isEqualTo(NationalBalanceResponse);
    }

    @Test
    public void shouldNotGetBalanceMsisdnNotKnownException() {
        exception.expect(AggregateUsageException.class);
        exception.expectMessage(String.format("National balance for msisdn: %s is not found", MSISDN));

        NationalBalanceResponse NationalBalanceResponse = createNationalBalanceResponse("201003", null);
        when(mockTipRestCallBuilder.get(NationalBalanceResponse.class)).thenReturn(Single.just(NationalBalanceResponse));

        Future<NationalBalanceResponse> balance = nationalBalanceService.getBalance(MSISDN, BPID, CID);
        ((AsyncResult) balance).invoke();
    }

    @Test
    public void shouldNotGetBalanceMsisdnUnknownErrorCode() {
        exception.expect(AggregateUsageException.class);
        exception.expectMessage("National balance call failed with error message: This is an error");

        NationalBalanceResponse NationalBalanceResponse = createNationalBalanceResponse("unknownCode", "This is an error");
        when(mockTipRestCallBuilder.get(NationalBalanceResponse.class)).thenReturn(Single.just(NationalBalanceResponse));

        Future<NationalBalanceResponse> balance = nationalBalanceService.getBalance(MSISDN, BPID, CID);
        ((AsyncResult) balance).invoke();
    }

    @Test
    public void shouldThrowAggregateUsageExceptionWhenIncompleteResponse() {
        exception.expect(AggregateUsageException.class);
        exception.expectMessage(String.format("Incomplete nationalbalance response for msisdn: %s", MSISDN));

        when(mockTipRestCallBuilder.get(NationalBalanceResponse.class)).thenReturn(Single.just(new NationalBalanceResponse()));

        Future<NationalBalanceResponse> balance = nationalBalanceService.getBalance(MSISDN, BPID, CID);
        ((AsyncResult) balance).invoke();
    }

    private NationalBalanceResponse createNationalBalanceResponse(String errorCode, String errorMessage) {
        Body body = new Body();

        NationalBalanceResponse NationalBalanceResponse = new NationalBalanceResponse();
        if (errorCode == null) {
            body.setACCOUNTBALANCE(new AccountBalance());
        }

        if (errorCode != null) {
            body.setErrorCode(errorCode);
            body.setErrorMessage(errorMessage);
        }

        GetBalanceOutput getBalanceOutput = new GetBalanceOutput();
        getBalanceOutput.setBody(body);
        NationalBalanceResponse.setGetBalanceOutput(getBalanceOutput);

        return NationalBalanceResponse;
    }

}