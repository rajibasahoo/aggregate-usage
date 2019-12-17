package nl.tele2.fez.stubs;

import java.net.URI;

abstract class StubService {

    protected abstract void setupStub(URI mapping);

    protected abstract void resetStubs();

}
