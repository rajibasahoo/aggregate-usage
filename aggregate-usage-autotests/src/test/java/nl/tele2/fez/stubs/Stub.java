package nl.tele2.fez.stubs;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

@Service
public class Stub {
    private static final int DEFAULT_STUBS_PORT = 3405;
    private static final String DEFAULT_STUBS_HOST = "localhost";

    private final StubStrategy stub;

    public Stub(
            @Value("${stubs.stub.host}") String host,
            @Value("${stubs.stub.port}") Integer port,
            RestTemplate restTemplate) throws IOException {
        final int wireMockPort = port != null ? port : DEFAULT_STUBS_PORT;

        //Start WireMock by test suite if we are running tests in local
        if (StringUtils.equals(host, DEFAULT_STUBS_HOST)) {
            System.out.println("Configuring WireMock with server: {}" + host + ":" + wireMockPort);
            stub = new WireMockServerStrategy(wireMockPort);
        } else {
            System.out.println("Configuring WireMock with client: {}" + host + ":" + wireMockPort);
            stub = new WireMockClientStrategy(StringUtils.defaultIfBlank(host, DEFAULT_STUBS_HOST), wireMockPort, restTemplate);
        }

        stub.initStub();
    }

    /**
     * Destroy method called by Spring Context on context shutdown.
     * In case we have WireMock server running for local testing, it will be shut down.
     */
    public void shutdown() {
        stub.shutdown();
    }

    public void verifyTimesGetCalled(int times, String url) {
        stub.verifyTimesGetCalled(times, url);
    }

    public void verifyCalledWith(String url, StringValuePattern... patterns) {
        RequestPatternBuilder patternBuilder = RequestPatternBuilder.newRequestPattern(RequestMethod.POST, UrlPattern.fromOneOf(null, null, url, null));
        for (StringValuePattern pattern : patterns) {
            patternBuilder.withRequestBody(pattern);
        }

        stub.verifyCalled(patternBuilder);
    }

    public void setupStub(String... mapping) {
        try {
            stub.setup(mapping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFixedDelay(Duration seconds) {
        stub.setFixedDelay(seconds);
    }

    public void reset() {
        stub.resetRequests();
        stub.setFixedDelay(Duration.ZERO);
    }

    @Override
    public String toString() {
        return "stub service";
    }
}
