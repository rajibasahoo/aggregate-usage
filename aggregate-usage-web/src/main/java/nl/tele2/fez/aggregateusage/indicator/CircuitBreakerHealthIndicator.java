package nl.tele2.fez.aggregateusage.indicator;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandMetrics;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class CircuitBreakerHealthIndicator implements HealthIndicator {
    static final Status UNSTABLE = new Status("UNSTABLE");

    public Health health() {
        for (HystrixCommandMetrics metrics : getHystrixInstances()) {
            HystrixCircuitBreaker circuitBreaker = getCircuitBreaker(metrics);
            if (circuitBreaker != null && circuitBreaker.isOpen()) {
                return Health.status(UNSTABLE).build();
            }
        }

        return Health.up().build();
    }

    Collection<HystrixCommandMetrics> getHystrixInstances() {
        return HystrixCommandMetrics.getInstances();
    }

    HystrixCircuitBreaker getCircuitBreaker(HystrixCommandMetrics metrics) {
        return HystrixCircuitBreaker.Factory.getInstance(metrics.getCommandKey());
    }
}
