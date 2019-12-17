package nl.tele2.fez.aggregateusage.controller;

import nl.tele2.fez.aggregateusage.Application;
import nl.tele2.fez.aggregateusage.TestData;
import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.service.AggregateUsageFactory;
import nl.tele2.fez.aggregateusage.service.MessagesService;
import nl.tele2.fez.aggregateusage.service.NationalBalanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Base64Utils;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static nl.tele2.fez.aggregateusage.util.AuthUtil.getTokenAuthorizedFor;
import static nl.tele2.fez.aggregateusage.TestConstants.BPID;
import static nl.tele2.fez.aggregateusage.TestConstants.CID;
import static nl.tele2.fez.aggregateusage.TestConstants.CUSTOMER_ID;
import static nl.tele2.fez.aggregateusage.TestConstants.MSISDN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class MessagesControllerTest {

    @MockBean
    private NationalBalanceService nationalBalanceService;

    @MockBean
    private AggregateUsageFactory aggregateUsageFactory;

    @MockBean
    private MessagesService messagesService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldHaveMandatoryAuthenticationSendMessageForBalance() throws Exception {
        mockMvc.perform(
                post("/messages/balance")
                        .header("BusinessProcessId", BPID)
                        .header("ConversationId", CID)
                        .header("jwtoken", getTokenAuthorizedFor(CUSTOMER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msisdn\": \"" + MSISDN + "\"}"))
                .andExpect(status().isUnauthorized());

        verify(messagesService, never()).sendBalance(eq(MSISDN), any(AggregateUsage.class));
    }

    @Test
    public void shouldSendMessageForBalance() throws Exception {
        when(nationalBalanceService.getBalance(anyString(), anyString(), anyString()))
                .thenReturn(completedFuture(null));

        when(aggregateUsageFactory.createNationalUsage(any()))
                .thenReturn(TestData.createSimpleAggregateUsage());

        mockMvc.perform(
                post("/messages/balance")
                        .header("BusinessProcessId", BPID)
                        .header("ConversationId", CID)
                        .header("jwtoken", getTokenAuthorizedFor(CUSTOMER_ID))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + Base64Utils.encodeToString("testuser:test".getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msisdn\": \"" + MSISDN + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("Status", equalTo("Success")));

        verify(messagesService).sendBalance(eq(MSISDN), any(AggregateUsage.class));
    }
}
