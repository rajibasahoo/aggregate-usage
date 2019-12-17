package nl.tele2.fez.aggregateusage.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tele2.fez.aggregateusage.dto.MsisdnsResponse;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageBadRequestException;
import nl.tele2.fez.aggregateusage.exception.MsisdnNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static nl.tele2.fez.aggregateusage.TestConstants.CUSTOMER_ID;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MsisdnsServiceTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private FezApiAdapter fezApiAdapter;

    private MsisdnsService msisdnsService;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        msisdnsService = new MsisdnsService(fezApiAdapter, "");

        MsisdnsResponse msisdnsResponse = createMsisdnsResponse("msisdn.json");
        when(fezApiAdapter.get(anyString(), eq(MsisdnsResponse.class), eq(CUSTOMER_ID)))
                .thenReturn(msisdnsResponse);
    }

    @Test
    public void getMsisdnStatusActive() {
        msisdnsService.verifyCorrectMsisdnStatus(CUSTOMER_ID, "31612345678");
    }

    @Test
    public void shouldThrowExceptionWhenInactive() {
        exception.expectMessage("MSISDN has incorrect status: Inactive");
        exception.expect(AggregateUsageBadRequestException.class);

        msisdnsService.verifyCorrectMsisdnStatus(CUSTOMER_ID, "31612345679");
    }

    @Test
    public void shouldThrowExceptionWhenSuspended() {
        exception.expectMessage("MSISDN has incorrect status: Suspended");
        exception.expect(AggregateUsageBadRequestException.class);

        msisdnsService.verifyCorrectMsisdnStatus(CUSTOMER_ID, "31612345670");
    }

    @Test(expected = RestClientException.class)
    public void getMsisdnException() {
        when(fezApiAdapter.get(anyString(), eq(MsisdnsResponse.class), eq(CUSTOMER_ID)))
                .thenThrow(new RestClientException(""));

        msisdnsService.verifyCorrectMsisdnStatus(CUSTOMER_ID, "31612345670");
    }

    @Test
    public void shouldThrowMsisdnNotFoundException() {
        exception.expect(MsisdnNotFoundException.class);
        exception.expectMessage("The provided MSISDN unknown does not belong to the supplied customerId customerId");

        msisdnsService.verifyCorrectMsisdnStatus(CUSTOMER_ID, "unknown");
    }

    private MsisdnsResponse createMsisdnsResponse(final String requestPath)
            throws IOException, URISyntaxException {
        String createJson = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(requestPath).toURI())));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        return objectMapper.readValue(createJson, MsisdnsResponse.class);
    }
}