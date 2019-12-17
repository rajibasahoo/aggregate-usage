package nl.tele2.fez.aggregateusage;

public enum ErrorCode {
    UNPARSABLE_XML("UNPARSABLE_XML"),
    UNKNOWN("UNKNOWN"),
    TIME_OUT("TIME_OUT"),
    UNAUTHORIZED("UNAUTHORIZED"),
    MSISDN_NOT_KNOWN_EXCEPTION("MSISDN_NOT_KNOWN_EXCEPTION"),
    INCOMPLETE_BODY("INCOMPLETE_BODY"),
    BAD_REQUEST("BAD_REQUEST"),
    MSISDN_NOT_FOUND("MSISDN_NOT_FOUND"),
    UNKNOWN_CAPACITY_UNIT("UNKNOWN_CAPACITY_UNIT"),
    UNKNOWN_RESPONSE("UNKNOWN_RESPONSE");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
