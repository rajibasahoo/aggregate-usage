package nl.tele2.fez.aggregateusage.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.dto.MsisdnsResponse;
import nl.tele2.fez.aggregateusage.enums.MsisdnStatus;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageBadRequestException;
import nl.tele2.fez.aggregateusage.exception.MsisdnNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class MsisdnsService {
    private final FezApiAdapter fezApi;
    private final String msisdnsEndpoint;

    @Autowired
    public MsisdnsService(
            FezApiAdapter fezApi,
            @Value("${msisdns.host}") String msisdnsHost
    ) {
        this.fezApi = fezApi;
        this.msisdnsEndpoint = msisdnsHost;
    }

    @HystrixCommand(groupKey = "AggregateUsage", commandKey = "getMsisdnStatus", ignoreExceptions = {MsisdnNotFoundException.class, AggregateUsageBadRequestException.class})
    public void verifyCorrectMsisdnStatus(String customerId, String msisdn) {
        MsisdnsResponse msisdnsResponse = getMsisdns(customerId);

        MsisdnStatus msisdnStatus;
        if (msisdnsResponse.getMsisdns().contains(msisdn)) {
            msisdnStatus = MsisdnStatus.ACTIVE;
        } else if (msisdnsResponse.getInactiveMsisdns().contains(msisdn)) {
            msisdnStatus = MsisdnStatus.INACTIVE;
        } else if (msisdnsResponse.getSuspendedMsisdns().contains(msisdn)) {
            msisdnStatus = MsisdnStatus.SUSPENDED;
        } else {
            throw new MsisdnNotFoundException("The provided MSISDN " + msisdn + " does not belong to the supplied customerId " + customerId);
        }

        if (msisdnStatus != MsisdnStatus.ACTIVE) {
            throw new AggregateUsageBadRequestException("MSISDN has incorrect status: " + msisdnStatus.getStatusDescription());
        }
    }

    private MsisdnsResponse getMsisdns(String customerId) {
        String url = msisdnsEndpoint + "/customers/{customerId}/msisdns";

        try {
            return fezApi.get(url, MsisdnsResponse.class, customerId);
        } catch (RestClientException ex) {
            log.error(String.format("getMsisdns encountered the following error: %s", ex.getMessage()), ex.getMessage(), ex);
            throw new RestClientException(String.format("getMsisdns encountered the following error: %s", ex.getMessage()));
        }
    }
}
