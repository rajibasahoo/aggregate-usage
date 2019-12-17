package nl.tele2.fez.aggregateusage.controller;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandMetrics;
import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.exception.HystrixException;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Profile("!prd")
@RequestMapping("/hystrix")
@RestController
public class HystrixController {

    @PostMapping(value = "/reset", produces = "text/plain")
    public void resetCircuitBreaker() throws HystrixException {
        log.info("resetting hystrix");
        resetHystrix(HystrixCommandMetrics.class, HystrixCircuitBreaker.Factory.class);
    }

    private void resetHystrix(Class<?>... classes) throws HystrixException {
        try {
            for (Class<?> clazz : classes) {
                Method method = clazz.getDeclaredMethod("reset");
                method.setAccessible(true);
                method.invoke(null);
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new HystrixException(e);
        }
    }
}
