package nl.tele2.fez.aggregateusage.service;

import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.dto.AggregateUsage;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.bundles.AggregatedDataBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.Bundle;
import nl.tele2.fez.aggregateusage.dto.messages.SendMessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Slf4j
@Component
public class MessagesService {

    private static final BigDecimal MEGABYTES_TO_GIGABYTES = new BigDecimal(1000);

    private final String messagingServiceHost;
    private final FezApiAdapter fezApi;
    private final CustomerDetailsService customerDetailsService;

    public MessagesService(
            @Value("${messaging.host}") String messagingServiceHost,
            CustomerDetailsService customerDetailsService,
            FezApiAdapter fezApi
    ) {
        this.fezApi = fezApi;
        this.customerDetailsService = customerDetailsService;
        this.messagingServiceHost = messagingServiceHost;
    }

    @HystrixCommand(groupKey = "AggregateUsage", ignoreExceptions = HttpClientErrorException.class)
    public void sendBalance(String msisdn, AggregateUsage usage) {
        String remainingAmount = formatDataUsage(usage.getData());
        String timeFrame = formatTimeFrame(usage.getData());
        sendMessage(createRequest(msisdn, remainingAmount, timeFrame));
    }

    private String formatTimeFrame(AggregatedDataBundle data) {
        final Optional<RepeatType> repeatType = data.getBundles().stream()
                .map(Bundle::getRepeatType)
                .reduce(this::mergeIfSame);

        return repeatType.flatMap(type -> {
            switch (type) {
                case DAILY:
                    return Optional.of(" vandaag");
                case BILL_CYCLE:
                case MONTHLY:
                    return Optional.of(" deze maand");
                default:
                    return Optional.empty();
            }
        }).orElse("");
    }

    private RepeatType mergeIfSame(RepeatType a, RepeatType b) {
        return a == b ? a : RepeatType.UNKNOWN;
    }

    private String formatDataUsage(AggregatedDataBundle dataBundle) {
        BigDecimal amountRemainingInMb = dataBundle.getRemaining();
        if (amountRemainingInMb.compareTo(MEGABYTES_TO_GIGABYTES) > 0) {
            return amountRemainingInMb.divide(MEGABYTES_TO_GIGABYTES, 2, RoundingMode.HALF_DOWN) + " GB";
        }

        return amountRemainingInMb + " MB";
    }

    private SendMessageRequest createRequest(String msisdn, String amountLeft, String timeFrame) {
        String billingAccountNumber = customerDetailsService.getBillingAccountNumber(msisdn);

        return new SendMessageRequest(
                new SendMessageRequest.Recipient(billingAccountNumber, msisdn),
                new SendMessageRequest.Message(
                        "SMS_FREE_TEXT",
                        ImmutableMap.of("CONTENT", formatMessage(amountLeft, timeFrame))
                )
        );
    }

    private String formatMessage(String amountLeft, String timeFrame) {
        return "Beste klant, je hebt" + timeFrame + " nog " + amountLeft + " data. We houden contact, Tele2";
    }

    private void sendMessage(SendMessageRequest requestBody) {
        fezApi.post(messagingServiceHost + "/sms", requestBody);
    }
}

