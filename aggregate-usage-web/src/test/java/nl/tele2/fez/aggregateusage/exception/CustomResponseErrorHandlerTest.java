package nl.tele2.fez.aggregateusage.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;

import javax.xml.bind.JAXBException;
import java.io.IOException;


public class CustomResponseErrorHandlerTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldHandleError() throws IOException, JAXBException {
        exception.expectMessage("Internal error in TIP routing");
        exception.expect(AggregateUsageException.class);
        CustomResponseErrorHandler errorHandler = new CustomResponseErrorHandler();

        errorHandler.handleError(new MockClientHttpResponse("<error>Internal error in TIP routing.</error>".getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}