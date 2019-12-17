package nl.tele2.fez.stubs;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.google.common.io.Files;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;

public class WireMockClientStrategy implements StubStrategy {
    private final String host;
    private final int port;
    private final WireMock client;
    private final RestTemplate restTemplate;

    public WireMockClientStrategy(String host, int port, RestTemplate restTemplate) {
        this.host = host;
        this.port = port;
        this.client = new WireMock(host, port);
        this.restTemplate = restTemplate;
    }

    @Override
    public void setup(String... fileNames) {
        System.out.println("Setting up the stub for the WireMock client");

        try {
            String deleteUrl = "http://" + host + ":" + port + "/__admin/mappings/";
            restTemplate.delete(deleteUrl);

            for (String fileName : fileNames) {
                File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
                String json = Files.toString(file, Charset.defaultCharset());
                String postUrl2 = "http://" + host + ":" + port + "/__admin/mappings/new";
                restTemplate.postForLocation(postUrl2, json);
            }
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong with setting up the files: " + e.getMessage());
        }

    }

    @Override
    public void initStub() throws IOException {
        File folder = new File(getClass().getClassLoader().getResource("__files").getFile());
        for (File file : folder.listFiles()) {
            String xml = Files.toString(file, Charset.defaultCharset());
            String url = "http://" + host + ":" + port + "/__admin/files/" + file.getName();
            restTemplate.put(url, xml);
        }

    }

    @Override
    public void shutdown() {
        // no shutdown needed
    }

    @Override
    public void verifyTimesPostCalled(int times, String url) {
        client.verifyThat(times, WireMock.postRequestedFor(WireMock.urlPathEqualTo(url)));
    }

    @Override
    public void verifyTimesGetCalled(int times, String url) {
        client.verifyThat(times, WireMock.getRequestedFor(WireMock.urlPathEqualTo(url)));
    }
    @Override
    public void resetRequests() {
        client.resetRequests();
    }

    @Override
    public void setFixedDelay(Duration delay) {
        client.setGlobalFixedDelayVariable((int) delay.getSeconds() * 1000);
    }

    @Override
    public void verifyCalled(RequestPatternBuilder builder) {
        client.verifyThat(builder);
    }
}
