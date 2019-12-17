package nl.tele2.fez.aggregateusage.exception;

import lombok.Getter;
import nl.tele2.fez.aggregateusage.ErrorCode;
import nl.tele2.response.commons.LogLevel;
import nl.tele2.response.exceptions.CustomIntegrationException;
import org.springframework.http.HttpStatus;

@Getter
public class AggregateUsageException extends CustomIntegrationException {

    public AggregateUsageException(String msg, HttpStatus statusCode, ErrorCode errorCode) {
        super(
                msg,
                statusCode,
                errorCode.getCode(),
                msg,
                msg,
                errorCode.getCode(),
                statusCode == HttpStatus.BAD_GATEWAY ? LogLevel.ERROR : LogLevel.WARN
        );
    }
}
