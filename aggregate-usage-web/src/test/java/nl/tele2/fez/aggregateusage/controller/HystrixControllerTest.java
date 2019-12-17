package nl.tele2.fez.aggregateusage.controller;

import nl.tele2.fez.aggregateusage.exception.HystrixException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HystrixControllerTest {

    private final HystrixController hystrixController = new HystrixController();

    @Test
    public void resetCircuitBreaker() throws HystrixException {
        hystrixController.resetCircuitBreaker();
        assertThat(true).isTrue();
    }
}