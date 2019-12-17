package nl.tele2.fez.aggregateusage.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import nl.tele2.fez.aggregateusage.tip.national.Body;
import nl.tele2.fez.aggregateusage.tip.national.NationalBalanceResponse;
import nl.tele2.fez.common.tip.rest.TipRestCallingService;
import nl.tele2.fez.common.tip.rest.TipTrackingInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

import static nl.tele2.fez.aggregateusage.ErrorCode.INCOMPLETE_BODY;
import static nl.tele2.fez.aggregateusage.ErrorCode.MSISDN_NOT_KNOWN_EXCEPTION;
import static nl.tele2.fez.aggregateusage.ErrorCode.UNKNOWN;

@Service
public class NationalBalanceService extends BalanceService<NationalBalanceResponse> {
    private static final String MSISDN_NOT_FOUND_ERROR_CODE = "201003";

    private final TipRestCallingService tipCallingService;
    private final String endpoint;

    public NationalBalanceService(TipRestCallingService tipCallingService, @Value("${aggregate-usage.national-endpoint}") String endpoint) {
        this.tipCallingService = tipCallingService;
        this.endpoint = endpoint;
    }

    @Override
    @HystrixCommand(groupKey = "AggregateUsage", commandKey = "getNationalBalance", ignoreExceptions = AggregateUsageException.class)
    public Future<NationalBalanceResponse> getBalance(String msisdn, String businessProcessId, String conversationId) {
        return new AsyncResult<NationalBalanceResponse>() {
            @Override
            public NationalBalanceResponse invoke() {
                NationalBalanceResponse response = tipCallingService.createForEndpoint(endpoint, new TipTrackingInformation(businessProcessId, conversationId))
                        .queryParam(MSISDN, msisdn)
                        .queryParam(EFFECTIVE_DATE, DateConverters.GET_BALANCE_DATE_TIME_FORMATTER.format(LocalDateTime.now()))
                        .queryParam(LIMITS_FLAG, "All")
                        .queryParam(INVOICE_LANGUAGE, "English")
                        .queryParam(ALLOCATIONS_FLAG, "All")
                        .get(NationalBalanceResponse.class).blockingGet();

                checkErrorResponse(response, msisdn);

                return response;
            }
        };
    }

    private Body unwrap(NationalBalanceResponse response, String msisdn) {
        if (response != null && response.getGetBalanceOutput() != null && response.getGetBalanceOutput().getBody() != null) {
            return response.getGetBalanceOutput().getBody();
        }
        throw new AggregateUsageException(String.format("Incomplete nationalbalance response for msisdn: %s", msisdn), HttpStatus.BAD_GATEWAY, INCOMPLETE_BODY);
    }

    private void checkErrorResponse(NationalBalanceResponse response, String msisdn) {
        Body body = unwrap(response, msisdn);

        String errorCode = body.getErrorCode();
        if (errorCode != null) {
            if (MSISDN_NOT_FOUND_ERROR_CODE.equals(errorCode)) {
                throw new AggregateUsageException(String.format("National balance for msisdn: %s is not found", msisdn), HttpStatus.NOT_FOUND, MSISDN_NOT_KNOWN_EXCEPTION);
            } else {
                throw new AggregateUsageException(String.format("National balance call failed with error message: %s", body.getErrorMessage()), HttpStatus.BAD_GATEWAY, UNKNOWN);
            }
        }
    }
}
