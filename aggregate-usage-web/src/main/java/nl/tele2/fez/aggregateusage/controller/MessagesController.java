package nl.tele2.fez.aggregateusage.controller;

import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.ErrorCode;
import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.dto.SendBalanceRequest;
import nl.tele2.fez.aggregateusage.dto.SendBalanceResponse;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import nl.tele2.fez.aggregateusage.service.AggregateUsageFactory;
import nl.tele2.fez.aggregateusage.service.MessagesService;
import nl.tele2.fez.aggregateusage.service.NationalBalanceService;
import nl.tele2.fez.aggregateusage.tip.national.NationalBalanceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@RestController
@AllArgsConstructor
public class MessagesController {
    private final NationalBalanceService nationalBalanceService;
    private final AggregateUsageFactory aggregateUsageFactory;
    private final MessagesService messagesService;

    @ApiOperation("Send message to msisdn with usage information")
    @PostMapping(value = "/messages/balance")
    @Timed(value = "rest.endpoint", extraTags = {"endpoint", "send-balance-message"}, histogram = true)
    public SendBalanceResponse sendBalanceMessage(
            @RequestBody SendBalanceRequest balanceRequest,
            @RequestHeader("BusinessProcessId") String businessProcessId,
            @RequestHeader("ConversationId") String conversationId,
            Principal loggedInUser) {
        if (loggedInUser == null || loggedInUser instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("Must be logged in");
        }

        Future<NationalBalanceResponse> balance = nationalBalanceService.getBalance(balanceRequest.getMsisdn(), businessProcessId, conversationId);

        try {
            AggregateUsage usage = aggregateUsageFactory.createNationalUsage(balance.get());
            messagesService.sendBalance(balanceRequest.getMsisdn(), usage);

            return new SendBalanceResponse("Success", "Message sent to customer");
        } catch (ExecutionException | InterruptedException e) { //NOSONAR
            log.error("Error getting aggregate usage.", e);
            Thread.currentThread().interrupt();
            throw new AggregateUsageException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN);
        }
    }

}
