package nl.tele2.fez.stubs;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

public class WireMockServerStrategy implements StubStrategy {
    private final WireMockServer server;

    public WireMockServerStrategy(int port) {
        this.server = new WireMockServer(port);
        this.server.start();
    }

    @Override
    public void setup(String... fileNames) throws IOException {
        server.resetMappings();

        for (String fileName : fileNames) {
            System.out.println("Setting up the stub for the WireMock server");
            File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
            byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

            server.addStubMapping(StubMapping.buildFrom(new String(encoded, Charset.defaultCharset())));
        }
    }

    @Override
    public void initStub() {

    }

    @Override
    public void shutdown() {
        server.shutdown();
    }

    @Override
    public void verifyTimesPostCalled(int times, String url) {
        server.verify(times, WireMock.postRequestedFor(WireMock.urlPathEqualTo(url)));
    }

    @Override
    public void verifyTimesGetCalled(int times, String url) {
        server.verify(times, WireMock.getRequestedFor(WireMock.urlPathEqualTo(url)));
    }

    @Override
    public void resetRequests() {
        server.resetRequests();
    }

    @Override
    public void setFixedDelay(Duration delay) {
        server.setGlobalFixedDelay((int) delay.getSeconds() * 1000);
    }

    @Override
    public void verifyCalled(RequestPatternBuilder builder) {
        server.verify(builder);
    }
}
