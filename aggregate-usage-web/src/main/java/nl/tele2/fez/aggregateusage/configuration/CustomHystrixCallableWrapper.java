package nl.tele2.fez.aggregateusage.configuration;

import io.jmnarloch.spring.boot.hystrix.context.HystrixCallableWrapper;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

public class CustomHystrixCallableWrapper implements HystrixCallableWrapper {

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        return new MDCAwareCallable<>(callable, MDC.getCopyOfContextMap());
    }

    class MDCAwareCallable<T> implements Callable<T> {
        private final Callable<T> callable;
        private final Map<String, String> mdcContextMap;

        MDCAwareCallable(Callable<T> callable, Map<String, String> mdcContextMap) {
            this.callable = callable;
            this.mdcContextMap = mdcContextMap;
        }

        @Override
        public T call() throws Exception {
            try {
                if (mdcContextMap != null) {
                    MDC.setContextMap(mdcContextMap);
                }

                return callable.call();
            } finally {
                MDC.clear();
            }
        }
    }
}
