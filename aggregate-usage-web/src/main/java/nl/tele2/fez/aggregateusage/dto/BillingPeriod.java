package nl.tele2.fez.aggregateusage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BillingPeriod {
    @ApiModelProperty(value = "Timestamp for first moment of the billing period, usually the first day of the month", example = "2018-12-31T08:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime startDate;

    @ApiModelProperty(value = "Timestamp for last moment of the billing period, usually the last day of the month", example = "2019-01-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime endDate;
}
