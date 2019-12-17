package nl.tele2.fez.aggregateusage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@Slf4j
@SpringBootApplication
@EnableCircuitBreaker
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}