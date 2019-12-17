package nl.tele2.fez.aggregateusage.indicator;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandMetrics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CircuitBreakerHealthIndicatorTest {

    @Mock
    private CircuitBreakerHealthIndicator healthIndicator;

    @Mock
    private HystrixCommandMetrics hystrixCommandMetrics;

    @Mock
    private HystrixCircuitBreaker hystrixCircuitBreaker;

    @Test
    public void shouldBeUp() {
        when(healthIndicator.health()).thenCallRealMethod();

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    public void shouldBeUnstable() {
        when(healthIndicator.health()).thenCallRealMethod();
        when(healthIndicator.getHystrixInstances()).thenReturn(Collections.singletonList(hystrixCommandMetrics));

        when(hystrixCircuitBreaker.isOpen()).thenReturn(true);
        when(healthIndicator.getCircuitBreaker(any())).thenReturn(hystrixCircuitBreaker);

        Health health = healthIndicator.health();
        assertEquals(CircuitBreakerHealthIndicator.UNSTABLE, health.getStatus());
    }
}
