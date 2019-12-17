package nl.tele2.fez.aggregateusage.service;

import lombok.experimental.UtilityClass;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateConverters {
    static final DateTimeFormatter GET_BALANCE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS[X]");

    private static final ZoneId AMSTERDAM_TIME = ZoneId.of("Europe/Amsterdam");

    public static LocalDateTime parseAndLocalize(String dateTime) {
        return ZonedDateTime.parse(dateTime, GET_BALANCE_DATE_TIME_FORMATTER)
                .withZoneSameInstant(AMSTERDAM_TIME)
                .toLocalDateTime();
    }

    public static LocalDateTime parseAndLocalize(XMLGregorianCalendar dateTime) {
        return dateTime.toGregorianCalendar().toZonedDateTime()
                .withZoneSameInstant(AMSTERDAM_TIME)
                .toLocalDateTime();
    }
}
