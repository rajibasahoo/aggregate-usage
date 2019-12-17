package nl.tele2.fez.aggregateusage.dto.bundles;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import nl.tele2.fez.aggregateusage.dto.BundleType;
import nl.tele2.fez.aggregateusage.dto.RepeatType;
import nl.tele2.fez.aggregateusage.dto.Unit;
import nl.tele2.fez.aggregateusage.dto.usage.Usage;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface Bundle {
    BundleType getType();

    String getName();

    String getProductName();

    String getCrmRef();

    Unit getUnit();

    RepeatType getRepeatType();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime getLastUsage();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime getStartDate();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime getEndDate();

    @JsonUnwrapped
    Usage getUsage();
}
