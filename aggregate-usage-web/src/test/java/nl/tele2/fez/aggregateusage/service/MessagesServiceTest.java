package nl.tele2.fez.aggregateusage.service;

import nl.tele2.fez.aggregateusage.TestData;
import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.messages.SendMessageRequest;
import nl.tele2.fez.aggregateusage.dto.usage.LimitedUsage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class MessagesServiceTest {

    @Mock
    private FezApiAdapter fezApi;

    @Mock
    private CustomerDetailsService customerDetailsService;

    private MessagesService messagesService;

    @Before
    public void setUp() throws Exception {
        messagesService = new MessagesService("http://example.com", customerDetailsService, fezApi);
    }

    @Test
    public void shouldSendBalanceToCorrectMsisdn() {
        messagesService.sendBalance("31612345678", TestData.createSimpleAggregateUsage());
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        verify(fezApi).post(anyString(), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getRecipient().getMsisdn(), equalTo("31612345678"));
    }

    @Test
    public void shouldSendBalanceAsFreeFormatTextMessage() {
        messagesService.sendBalance("31612345678", TestData.createSimpleAggregateUsage());
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        verify(fezApi).post(anyString(), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getMessage().getTemplate(), equalTo("SMS_FREE_TEXT"));
    }

    @Test
    public void shouldSendBalanceWithUsageInGbInContent() {
        messagesService.sendBalance("31612345678", TestData.createSimpleAggregateUsage());
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        verify(fezApi).post(anyString(), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getMessage().getContext().get("CONTENT"), containsString("je hebt vandaag nog 1.23 GB data"));
    }

    @Test
    public void shouldSendBalanceWithUsageInMbInContentWhenLessThanOneGb() {
        AggregateUsage usage = TestData.createSimpleAggregateUsage(RepeatType.BILL_CYCLE, new LimitedUsage(new BigDecimal(5000), new BigDecimal(916.23D)));

        messagesService.sendBalance("31612345678", usage);
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        verify(fezApi).post(anyString(), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getMessage().getContext().get("CONTENT"), containsString("je hebt deze maand nog 916 MB data"));
    }

    @Test
    public void shouldSendBalanceWithoutTimeFrameForSomeRandomValue() {
        AggregateUsage usage = TestData.createSimpleAggregateUsage(RepeatType.ONE_TIME, new LimitedUsage(new BigDecimal(5000), new BigDecimal(916.23D)));

        messagesService.sendBalance("31612345678", usage);
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        verify(fezApi).post(anyString(), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getMessage().getContext().get("CONTENT"), containsString("je hebt nog 916 MB data"));
    }

    @Test
    public void shouldSendBalanceWithCustomerBillingAccountNumber() {
        when(customerDetailsService.getBillingAccountNumber("31612345678"))
                .thenReturn("FOO-123");

        messagesService.sendBalance("31612345678", TestData.createSimpleAggregateUsage());
        ArgumentCaptor<SendMessageRequest> argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);

        verify(fezApi).post(anyString(), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getRecipient().getCustomerId(), equalTo("FOO-123"));
    }
}