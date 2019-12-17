package nl.tele2.fez.aggregateusage.dto.messages;

import lombok.Data;

import java.util.Map;

@Data
public class SendMessageRequest {
    private final Recipient recipient;
    private final Message message;

    @Data
    public static class Recipient {
        private final String customerId;
        private final String msisdn;
    }

    @Data
    public static class Message {
        private final String template;
        private final Map<String, String> context;
    }
}
