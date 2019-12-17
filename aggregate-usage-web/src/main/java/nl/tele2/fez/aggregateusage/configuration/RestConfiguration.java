package nl.tele2.fez.aggregateusage.configuration;

import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.exception.CustomResponseErrorHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBException;


@Slf4j
@Configuration
public class RestConfiguration {

    private HttpClient createHttpClient() {
        return HttpClients.custom()
                .useSystemProperties()
                .setRetryHandler(new StandardHttpRequestRetryHandler(3, false))
                .build();
    }

    @Bean
    @Primary
    public RestTemplate restTemplate(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${aggregate-usage.username}") String username,
            @Value("${aggregate-usage.password}") String password
    ) throws JAXBException {
        return restTemplateBuilder
                .basicAuthorization(username, password)
                .errorHandler(new CustomResponseErrorHandler())
                .build();
    }

    @Bean
    public RestTemplate fezRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                        @Value("${fez.username}") String fezUsername,
                                        @Value("${fez.password}") String fezPassword) {
        return restTemplateBuilder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(createHttpClient()))
                .basicAuthorization(fezUsername, fezPassword)
                .setConnectTimeout(15000)
                .setReadTimeout(15000)
                .build();
    }
}
