package nl.tele2.fez.aggregateusage.service;

import nl.tele2.fez.aggregateusage.dto.customerdetails.CustomerDetailsResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CustomerDetailsServiceTest {
    private static final String BOGUS_BILLING_ACCOUNT_ID = "1";

    @Mock
    private FezApiAdapter fezApi;

    private CustomerDetailsService customerDetailsService;

    @Before
    public void setUp() throws Exception {
        customerDetailsService = new CustomerDetailsService("http://example.com", fezApi);
    }

    @Test
    public void shouldRetrieveCustomerId() {
        when(fezApi.get(eq("http://example.com/msisdns/{msisdn}/customer"), any(), eq("foo")))
                .thenReturn(createCustomerDetailsResponse());

        assertThat(customerDetailsService.getBillingAccountNumber("foo"), equalTo("You got me"));
    }

    @Test
    public void shouldFallbackToBogusCustomerId() {
        assertThat(customerDetailsService.defaultBillingAccountNumber("foo"), equalTo(BOGUS_BILLING_ACCOUNT_ID));
    }

    private CustomerDetailsResponse createCustomerDetailsResponse() {
        CustomerDetailsResponse response = new CustomerDetailsResponse();
        response.setAccountId("You got me");
        return response;
    }
}