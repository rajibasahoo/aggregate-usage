package nl.tele2.fez.aggregateusage.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Getter
@AllArgsConstructor
public class SendBalanceResponse {
    private final String status;
    private final String statusMessage;
}
