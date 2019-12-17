package nl.tele2.fez.aggregateusage.controller;

import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.ErrorCode;
import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import nl.tele2.fez.aggregateusage.service.AggregateUsageFactory;
import nl.tele2.fez.aggregateusage.service.MsisdnsService;
import nl.tele2.fez.aggregateusage.service.NationalBalanceService;
import nl.tele2.fez.aggregateusage.service.RestOfWorldBalanceService;
import nl.tele2.fez.aggregateusage.tip.national.NationalBalanceResponse;
import nl.tele2.fez.aggregateusage.tip.restofworld.RestOfWorldBalanceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@RestController
@AllArgsConstructor
public class AggregateUsageController {

    private final NationalBalanceService nationalBalanceService;
    private final RestOfWorldBalanceService restOfWorldBalanceService;
    private final MsisdnsService msisdnsService;
    private final AggregateUsageFactory aggregateUsageFactory;

    @ApiOperation("Retrieve aggregated usage for the given customerId and Msisdn")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @GetMapping(value = "/customers/{customerId}/msisdns/{msisdn}")
    @Timed(value = "rest.endpoint", extraTags = {"endpoint", "aggregate-usage"}, histogram = true)
    public AggregateUsage getAggregateUsage(
            @PathVariable String customerId,
            @PathVariable String msisdn,
            @RequestHeader("BusinessProcessId") String businessProcessId,
            @RequestHeader("ConversationId") String conversationId) {
        try {
            msisdnsService.verifyCorrectMsisdnStatus(customerId, msisdn);

            Future<NationalBalanceResponse> nationalBalanceFuture = nationalBalanceService.getBalance(msisdn, businessProcessId, conversationId);
            Future<RestOfWorldBalanceResponse> restOfWorldBalanceFuture = restOfWorldBalanceService.getBalance(msisdn, businessProcessId, conversationId);

            NationalBalanceResponse nationalBalance = nationalBalanceFuture.get();
            RestOfWorldBalanceResponse restOfWorldBalance = restOfWorldBalanceFuture.get();

            return aggregateUsageFactory.createAggregateUsage(nationalBalance, restOfWorldBalance);
        } catch (ExecutionException | InterruptedException e) { //NOSONAR
            log.error("Error getting aggregate usage.", e);
            Thread.currentThread().interrupt();
            throw new AggregateUsageException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN);
        }
    }
}
