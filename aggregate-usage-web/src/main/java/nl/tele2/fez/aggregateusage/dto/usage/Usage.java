package nl.tele2.fez.aggregateusage.dto.usage;

public interface Usage {
    boolean isUnlimited();
    Usage aggregate(Usage other);
}
