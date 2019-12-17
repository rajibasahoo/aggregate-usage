package nl.tele2.fez.aggregateusage.exception;

import nl.tele2.response.commons.LogLevel;
import nl.tele2.response.exceptions.CustomInternalException;
import org.springframework.http.HttpStatus;

import static nl.tele2.fez.aggregateusage.ErrorCode.MSISDN_NOT_FOUND;

public class MsisdnNotFoundException extends CustomInternalException {

    public MsisdnNotFoundException(String message) {
        super(
                message,
                HttpStatus.NOT_FOUND,
                MSISDN_NOT_FOUND.getCode(),
                message,
                message,
                MSISDN_NOT_FOUND.getCode(),
                LogLevel.WARN
        );
    }
}
