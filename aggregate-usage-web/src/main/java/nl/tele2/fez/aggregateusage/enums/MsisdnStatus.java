package nl.tele2.fez.aggregateusage.enums;

import lombok.Getter;

public enum MsisdnStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended");

    @Getter
    private final String statusDescription;

    MsisdnStatus(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
