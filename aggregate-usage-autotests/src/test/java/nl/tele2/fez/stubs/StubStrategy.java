package nl.tele2.fez.stubs;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import java.io.IOException;
import java.time.Duration;

interface StubStrategy {
    void setup(String... fileNames) throws IOException;

    void initStub() throws IOException;

    void shutdown();

    void verifyTimesPostCalled(int times, String url);

    void verifyTimesGetCalled(int times, String url);

    void resetRequests();

    void setFixedDelay(Duration delay);

    void verifyCalled(RequestPatternBuilder builder);
}
