package nl.tele2.fez.aggregateusage.exception;

import nl.tele2.response.commons.LogLevel;
import nl.tele2.response.exceptions.CustomInternalException;
import org.springframework.http.HttpStatus;

import static nl.tele2.fez.aggregateusage.ErrorCode.BAD_REQUEST;

public class AggregateUsageBadRequestException extends CustomInternalException {
    public AggregateUsageBadRequestException(String msg) {
        super(
                msg,
                HttpStatus.BAD_REQUEST,
                BAD_REQUEST.getCode(),
                msg,
                msg,
                BAD_REQUEST.getCode(),
                LogLevel.WARN
        );
    }
}
