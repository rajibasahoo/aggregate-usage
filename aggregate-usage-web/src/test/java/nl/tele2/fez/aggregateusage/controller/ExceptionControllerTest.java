package nl.tele2.fez.aggregateusage.controller;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import nl.tele2.fez.aggregateusage.ErrorCode;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import nl.tele2.response.commons.ErrorResponseBuilder;
import nl.tele2.response.model.BaseResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ExceptionControllerTest {

    @InjectMocks
    private ExceptionController exceptionController;

    @Before
    public void setUp() {
        ErrorResponseBuilder builder = new ErrorResponseBuilder();

        ReflectionTestUtils.setField(builder, "apiName", "AggregateUsage API");
        ReflectionTestUtils.setField(exceptionController, "builder", builder);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void shouldHandleHystrixTimeOutException() {
        ResponseEntity<BaseResponse> response = exceptionController
                .handle(new TimeoutException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getDeveloperMessage()).contains("Call to TIP has timed-out");
    }

    @Test
    public void shouldHandleHttpClientErrorExceptionNotFound() {

        ResponseEntity<BaseResponse> response =
                exceptionController.handle(new HttpClientErrorException(HttpStatus.NOT_FOUND, "not found", "not found".getBytes(), Charset.defaultCharset()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getDeveloperMessage()).contains("Not found");
    }

    @Test
    public void shouldHandleHttpClientErrorExceptionWhenOtherThanNotFound() {
        ResponseEntity<BaseResponse> response = exceptionController.handle(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST, "Bad Request", "Some weird error".getBytes(), Charset.defaultCharset()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getDeveloperMessage()).contains("Bad Request");
    }

    @Test
    public void shouldHandleHttpMessageNotReadableException() {
        ResponseEntity<BaseResponse> response = exceptionController.handle(new HttpMessageNotReadableException("xml could not be parsed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getDeveloperMessage()).contains("Unable to parse response from TIP.");
    }

    @Test
    public void shouldHandleRestClientException() {
        ResponseEntity<BaseResponse> response = exceptionController.handle(new RestClientException("RestClient Exception"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getDeveloperMessage()).contains("RestClient Exception");
    }

    @Test
    public void shouldHandleExecutionExceptionWrapsHystrixRuntimeException() {
        HystrixRuntimeException hystrixRuntimeException = mock(HystrixRuntimeException.class);

        when(hystrixRuntimeException.getCause()).thenReturn(new AggregateUsageException("AU exception", HttpStatus.BAD_GATEWAY, ErrorCode.MSISDN_NOT_KNOWN_EXCEPTION));

        ExecutionException executionException = new ExecutionException("a message", hystrixRuntimeException);

        ResponseEntity<BaseResponse> response = exceptionController.handle(executionException);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getDeveloperMessage()).contains("AU exception");
    }

    @Test
    public void shouldHandleExecutionException() {
        ExecutionException executionException = new ExecutionException("a message", null);

        ResponseEntity<BaseResponse> response = exceptionController.handle(executionException);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getDeveloperMessage()).contains("a message");
    }

}