package nl.tele2.fez.aggregateusage.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;

import static nl.tele2.fez.aggregateusage.ErrorCode.UNKNOWN_RESPONSE;

@Slf4j
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

    private final Unmarshaller tipErrorUnmarshaller;

    public CustomResponseErrorHandler() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(TipError.class);
        tipErrorUnmarshaller = jaxbContext.createUnmarshaller();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String responseString = new String(getResponseBody(response));
        if (responseString.contains("<error>")) {
            try {
                TipError tipError = (TipError) tipErrorUnmarshaller.unmarshal(new StringReader(responseString));
                throw new AggregateUsageException(tipError.getError(), HttpStatus.BAD_GATEWAY, UNKNOWN_RESPONSE);
            } catch (JAXBException ex) {
                log.error("Exception while unmarshalling error response: ", ex);
            }
        }
        super.handleError(response);
    }
}
