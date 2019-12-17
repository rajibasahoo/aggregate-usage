package nl.tele2.fez.aggregateusage.service;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.dto.customerdetails.CustomerDetailsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerDetailsService {
    private final String customerDetailsHost;
    private final FezApiAdapter fezApi;

    public CustomerDetailsService(
            @Value("${details.host}") String customerDetailsHost,
            FezApiAdapter fezApi
    ) {
        this.fezApi = fezApi;
        this.customerDetailsHost = customerDetailsHost;
    }

    @HystrixCommand(groupKey = "AggregateUsage", fallbackMethod = "defaultBillingAccountNumber")
    public String getBillingAccountNumber(String msisdn) {
        String url = customerDetailsHost + "/msisdns/{msisdn}/customer";
        return fezApi.get(url, CustomerDetailsResponse.class, msisdn).getAccountId();
    }

    @VisibleForTesting
    String defaultBillingAccountNumber(String msisdn) {
        log.warn("Using fallback method for BAN: no activity will be registered in Siebel!");
        return "1";
    }

}
