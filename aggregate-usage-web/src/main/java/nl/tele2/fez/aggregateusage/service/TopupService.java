package nl.tele2.fez.aggregateusage.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.dto.TopupInfo;
import nl.tele2.fez.aggregateusage.tip.national.AccountBalance;
import nl.tele2.fez.aggregateusage.tip.restofworld.DocTypeRefTnsProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Slf4j
@Service
public class TopupService {
    private static final String ROAMING_EU_BUNDLE_NAME = "Roaming EU";

    private final FezApiAdapter fezApi;
    private final String topupsEndpoint;

    private final LoadingCache<String, TopupInfo> topupCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build(
                    new CacheLoader<String, TopupInfo>() {
                        public TopupInfo load(String key) {
                            return getTopup(key);
                        }

                        private TopupInfo getTopup(String productId) {
                            String url = topupsEndpoint + "/topups/{productId}";
                            return fezApi.get(url, TopupInfo.class, productId);
                        }
                    });

    @Autowired
    public TopupService(
            FezApiAdapter fezApi,
            @Value("${topups.host}") String topupsHost
    ) {
        this.fezApi = fezApi;
        this.topupsEndpoint = topupsHost;
    }

    @HystrixCommand(groupKey = "AggregateUsage", commandKey = "getZoneForRowBundle", fallbackMethod = "fallbackZoneForRowBundle")
    public String getZoneForRowBundle(DocTypeRefTnsProduct bundle) {
        if (ROAMING_EU_BUNDLE_NAME.equals(bundle.getName())) {
            return bundle.getName();
        }

        try {
            TopupInfo topup = topupCache.getUnchecked(Long.toString(bundle.getProductID()));
            return topup.getZoneId().toLowerCase();
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                if (e.getCause() instanceof HttpClientErrorException) {
                    return fallbackZoneForRowBundle(bundle, e.getCause());
                }
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    @HystrixCommand(groupKey = "AggregateUsage", commandKey = "getNameForBundle", fallbackMethod = "fallbackNameForBundle")
    public String getNameForBundle(AccountBalance.BucketAllocations.Item bundle) {
        try {
            TopupInfo topup = topupCache.getUnchecked(defaultIfBlank(bundle.getProductName(), bundle.getBucketCategory()));
            return topup.getProductDescription();
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                if (e.getCause() instanceof HttpClientErrorException) {
                    return fallbackNameForBundle(bundle, e.getCause());
                }
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public String fallbackNameForBundle(AccountBalance.BucketAllocations.Item bundle, Throwable ex) {
        log.info("Could not find zone for topup with id {}, using invoice text", bundle.getBucketCategory(), ex);
        return bundle.getBucketInvoiceText();
    }

    public String fallbackZoneForRowBundle(DocTypeRefTnsProduct bundle, Throwable ex) {
        log.info("Could not find zone for topup with id {} and name \"{}\", guessing from ThresholdMessage", bundle.getProductID(), bundle.getName(), ex);
        return bundle.getThresholdMessage().stream()
                .filter(s -> s.contains("TH_100_"))
                .findFirst()
                .map(s -> s.replace("TH_100_", ""))
                .map(String::toLowerCase)
                .orElse("unknown");
    }
}
