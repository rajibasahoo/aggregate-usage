package nl.tele2.fez.aggregateusage.dto;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public enum RepeatType {
    UNKNOWN("Unknown"),
    ONE_TIME("Single"),
    DAILY("Daily"),
    MONTHLY("Monthly", "Calendar Month"),
    BILL_CYCLE("Bill Cycle", "Bill Cycle Month");

    private final List<String> ocsValues;

    RepeatType(String... ocsValues) {
        this.ocsValues = asList(ocsValues);
    }

    public static RepeatType parse(String ocsValue) {
        return Arrays.stream(RepeatType.values())
                .filter(it -> it.ocsValues.contains(ocsValue))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot parse repeat type \"%s\"", ocsValue)));
    }
}
