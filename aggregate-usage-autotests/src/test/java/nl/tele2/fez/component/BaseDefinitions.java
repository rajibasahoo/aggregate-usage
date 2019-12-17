package nl.tele2.fez.component;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

public class BaseDefinitions {

    @Autowired
    protected RestTemplate restTemplate;

    @Value("${aggregate-usage.host}")
    private String aggregateUsageHost;

    @Value("${aggregate-usage.port}")
    private int aggregateUsagePort;

    public int responseStatusCode;
    public String savedResponseBody;

    void doCall(String path, HttpMethod method, Object body) {
        doCall(path, method, body, "testuser", "test");
    }

    private void doCall(String path, HttpMethod method, Object body, String username, String password) {
        URI uri = UriComponentsBuilder.fromHttpUrl(buildUrl(path)).build().encode().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add("BusinessProcessId", "aaa");
        headers.add("ConversationId", "bbb");
        if (username != null && password != null) {
            headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes())));
        }

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, new HttpEntity<>(body, headers), String.class);
            this.responseStatusCode = responseEntity.getStatusCode().value();
            this.savedResponseBody = responseEntity.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            savedResponseBody = ex.getResponseBodyAsString();
            responseStatusCode = ex.getStatusCode().value();
        }
    }

    private String buildUrl(final String path) {
        try {
            return new URIBuilder()
                    .setScheme("http")
                    .setHost(aggregateUsageHost)
                    .setPort(aggregateUsagePort)
                    .setPath(path).build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to build url for Aggregate Usage endpoint", e);
        }
    }
}
