package nl.tele2.fez.aggregateusage.service;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class FezApiAdapter {
    static final String BUSINESS_PROCESS_ID_HEADER = "BusinessProcessId";
    static final String CONVERSATION_ID_HEADER = "ConversationId";

    private final RestTemplate fezRestTemplate;

    @Autowired
    public FezApiAdapter(@Qualifier("fezRestTemplate") RestTemplate fezRestTemplate) {
        this.fezRestTemplate = fezRestTemplate;
    }

    public <T> void post(String url, T requestBody) {
        HttpHeaders headers = createCommonHeaders();
        HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);

        fezRestTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public <T> T get(String url, Class<T> responseClass, Object... urlParams) {
        HttpHeaders headers = createCommonHeaders();
        HttpEntity<T> entity = new HttpEntity<>(headers);

        return fezRestTemplate.exchange(url, HttpMethod.GET, entity, responseClass, urlParams).getBody();
    }

    private HttpHeaders createCommonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(BUSINESS_PROCESS_ID_HEADER, MDC.get(BUSINESS_PROCESS_ID_HEADER));
        headers.set(CONVERSATION_ID_HEADER, UUID.randomUUID().toString());
        return headers;
    }
}
