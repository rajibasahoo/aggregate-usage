package nl.tele2.fez;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@PropertySource("classpath:autotests-${spring.profiles.active}.properties")
@ComponentScan("nl.tele2.fez.stubs")
@Configuration
public class Config {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }
}
