package nl.tele2.fez.aggregateusage.controller;

import com.netflix.hystrix.Hystrix;
import lombok.extern.slf4j.Slf4j;
import nl.tele2.fez.aggregateusage.ErrorCode;
import nl.tele2.fez.aggregateusage.exception.AggregateUsageException;
import nl.tele2.response.commons.InternalErrors;
import nl.tele2.response.commons.LogLevel;
import nl.tele2.response.exceptions.CustomIntegrationException;
import nl.tele2.response.handlers.CustomExceptionHandler;
import nl.tele2.response.model.BaseResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ExceptionController extends CustomExceptionHandler {

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<BaseResponse> handle(TimeoutException ex) {
        String message = "Call to TIP has timed-out";
        return handleIntegrationException(
                new CustomIntegrationException(message, HttpStatus.BAD_GATEWAY,
                        ErrorCode.TIME_OUT.getCode(), message, null, null, LogLevel.WARN),
                null
        );
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<BaseResponse> handle(HttpClientErrorException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            String message = "Not found";
            Hystrix.reset();
            return handleIntegrationException(
                    new CustomIntegrationException(message, HttpStatus.NOT_FOUND,
                            InternalErrors.RESOURCE_NOT_FOUND.errorCode(), message, null, null, LogLevel.WARN),
                    null
            );
        } else {
            return handleIntegrationException(
                    new CustomIntegrationException(
                            ex.getMessage(), HttpStatus.BAD_GATEWAY, ErrorCode.UNKNOWN.getCode(),
                            ex.getMessage(), null, null, LogLevel.WARN),
                    null
            );
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse> handle(HttpMessageNotReadableException ex) {
        String message = "Unable to parse response from TIP.";
        return handleIntegrationException(new CustomIntegrationException(message,
                HttpStatus.BAD_GATEWAY, ErrorCode.UNPARSABLE_XML.getCode(), message, null, null, LogLevel.ERROR), null);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<BaseResponse> handle(RestClientException ex) {
        return handleIntegrationException(new CustomIntegrationException(ex.getMessage(),
                HttpStatus.BAD_GATEWAY, ErrorCode.UNKNOWN.getCode(), ex.getMessage(), null, null, LogLevel.ERROR), null);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<BaseResponse> handle(ExecutionException ex) {
        log.info("Something went wrong ExecutionException: ", ex);
        Throwable cause = ex.getCause();
        if (cause != null) {
            Throwable wrappedCause = cause.getCause();
            if (wrappedCause instanceof AggregateUsageException) {
                return handleIntegrationException((AggregateUsageException) wrappedCause, null);
            }
        }

        return handleIntegrationException(new CustomIntegrationException(ex.getMessage(),
                HttpStatus.BAD_GATEWAY, ErrorCode.UNKNOWN.getCode(), ex.getMessage(), null, null, LogLevel.ERROR), null);
    }

}
