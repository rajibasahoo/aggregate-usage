package nl.tele2.fez.aggregateusage.controller;

import nl.tele2.fez.aggregateusage.Application;
import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageBadRequestException;
import nl.tele2.fez.aggregateusage.service.AggregateUsageFactory;
import nl.tele2.fez.aggregateusage.service.MsisdnsService;
import nl.tele2.fez.aggregateusage.service.NationalBalanceService;
import nl.tele2.fez.aggregateusage.service.RestOfWorldBalanceService;
import nl.tele2.fez.aggregateusage.tip.national.NationalBalanceResponse;
import nl.tele2.fez.aggregateusage.tip.restofworld.RestOfWorldBalanceResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static nl.tele2.fez.aggregateusage.util.AuthUtil.getTokenAuthorizedFor;
import static nl.tele2.fez.aggregateusage.TestConstants.BPID;
import static nl.tele2.fez.aggregateusage.TestConstants.CID;
import static nl.tele2.fez.aggregateusage.TestConstants.CUSTOMER_ID;
import static nl.tele2.fez.aggregateusage.TestConstants.MSISDN;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class AggregateUsageControllerTest {


    @MockBean
    private NationalBalanceService nationalBalanceService;

    @MockBean
    private RestOfWorldBalanceService restOfWorldBalanceService;

    @MockBean
    private MsisdnsService msisdnsService;

    @MockBean
    private AggregateUsageFactory aggregateUsageFactory;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldGetAggregateUsage() throws Exception {
        NationalBalanceResponse nationalBalanceResponse = new NationalBalanceResponse();
        RestOfWorldBalanceResponse restOfWorldResponse = new RestOfWorldBalanceResponse();

        when(nationalBalanceService.getBalance(MSISDN, BPID, CID)).thenReturn(completedFuture(nationalBalanceResponse));
        when(restOfWorldBalanceService.getBalance(MSISDN, BPID, CID)).thenReturn(completedFuture(restOfWorldResponse));
        when(aggregateUsageFactory.createAggregateUsage(eq(nationalBalanceResponse), eq(restOfWorldResponse))).thenReturn(new AggregateUsage());

        mockMvc.perform(
                get("/customers/" + CUSTOMER_ID + "/msisdns/" + MSISDN)
                        .header("BusinessProcessId", BPID)
                        .header("ConversationId", CID)
                        .header("jwtoken", getTokenAuthorizedFor(CUSTOMER_ID)))
                .andExpect(status().isOk());

        verify(msisdnsService).verifyCorrectMsisdnStatus(CUSTOMER_ID, MSISDN);
    }

    @Test
    public void shouldNotGetAggregateUsageWhenMsisdnHasIncorrectStatus() throws Exception {
        doThrow(new AggregateUsageBadRequestException("exception")).when(msisdnsService).verifyCorrectMsisdnStatus(CUSTOMER_ID, MSISDN);

        mockMvc.perform(
                get("/customers/" + CUSTOMER_ID + "/msisdns/" + MSISDN)
                        .header("BusinessProcessId", BPID)
                        .header("ConversationId", CID)
                        .header("jwtoken", getTokenAuthorizedFor(CUSTOMER_ID)))
                .andExpect(status().isBadRequest());
    }
}
