package nl.tele2.fez.aggregateusage.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import nl.tele2.fez.aggregateusage.tip.restofworld.GetAllBucketsResponse;
import nl.tele2.fez.aggregateusage.tip.restofworld.RestOfWorldBalanceResponse;
import nl.tele2.fez.common.tip.rest.TipRestCallingService;
import nl.tele2.fez.common.tip.rest.TipTrackingInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

import static nl.tele2.fez.aggregateusage.ErrorCode.INCOMPLETE_BODY;

@Service
public class RestOfWorldBalanceService extends BalanceService<RestOfWorldBalanceResponse> {
    public static final String NO_DATA_DESCRIPTION = "NODATA";
    public static final int ERROR_RESPONSE_CODE = 3002;

    private final TipRestCallingService tipCallingService;
    private final String endpoint;

    public RestOfWorldBalanceService(TipRestCallingService tipCallingService, @Value("${aggregate-usage.restofworld-endpoint}") String endpoint) {
        this.tipCallingService = tipCallingService;
        this.endpoint = endpoint;
    }

    @HystrixCommand(groupKey = "AggregateUsage", commandKey = "getRestOfWorldBalance", ignoreExceptions = AggregateUsageException.class)
    @Override
    public Future<RestOfWorldBalanceResponse> getBalance(String msisdn, String businessProcessId, String conversationId) {
        return new AsyncResult<RestOfWorldBalanceResponse>() {
            @Override
            public RestOfWorldBalanceResponse invoke() {
                RestOfWorldBalanceResponse response = tipCallingService.createForEndpoint(endpoint, new TipTrackingInformation(businessProcessId, conversationId))
                        .queryParam(MSISDN, msisdn)
                        .get(RestOfWorldBalanceResponse.class)
                        .blockingGet();

                return validateResponse(response, msisdn);
            }
        };
    }

    private RestOfWorldBalanceResponse validateResponse(RestOfWorldBalanceResponse response, String msisdn) {
        GetAllBucketsResponse getAllBucketsResponse = response.getGetAllBucketsResponse();
        if (getAllBucketsResponse == null) {
            throw new AggregateUsageException(String.format("Incomplete restOfWorld response for msisdn: %s", msisdn), HttpStatus.BAD_GATEWAY, INCOMPLETE_BODY);
        }
        if (ERROR_RESPONSE_CODE == getAllBucketsResponse.getCode() && NO_DATA_DESCRIPTION.equals(getAllBucketsResponse.getDescription())) {
            return null;
        }
        return response;
    }
}
