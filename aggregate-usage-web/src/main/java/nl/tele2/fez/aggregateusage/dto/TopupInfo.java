package nl.tele2.fez.aggregateusage.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopupInfo {
    private String id;
    private String productDescription;
    private int sizeInMb;
    private String durationHours;
    private BigDecimal price;
    private String zoneId;
}
