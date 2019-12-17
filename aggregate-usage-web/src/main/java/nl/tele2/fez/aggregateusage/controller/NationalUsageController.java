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
import nl.tele2.fez.aggregateusage.tip.national.NationalBalanceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping
@AllArgsConstructor
public class NationalUsageController {

    private final NationalBalanceService nationalBalanceService;
    private final MsisdnsService msisdnsService;
    private final AggregateUsageFactory aggregateUsageFactory;

    @ApiOperation("Retrieve national usage for the given customerId and Msisdn")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @GetMapping(value = "/customers/{customerId}/msisdns/{msisdn}/national")
    @Timed(value = "rest.endpoint", extraTags = {"endpoint", "national-usage"}, histogram = true)
    public AggregateUsage getNationalUsage(
            @PathVariable String customerId,
            @PathVariable String msisdn,
            @RequestHeader("BusinessProcessId") String businessProcessId,
            @RequestHeader("ConversationId") String conversationId) {
        try {
            msisdnsService.verifyCorrectMsisdnStatus(customerId, msisdn);

            NationalBalanceResponse nationalBalance = nationalBalanceService.getBalance(msisdn, businessProcessId, conversationId).get();

            return aggregateUsageFactory.createNationalUsage(nationalBalance);
        } catch (ExecutionException | InterruptedException e) { //NOSONAR
            log.error("Error getting aggregate usage.", e);
            Thread.currentThread().interrupt();
            throw new AggregateUsageException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN);
        }
    }

}
