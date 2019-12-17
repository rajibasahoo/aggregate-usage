package nl.tele2.fez.aggregateusage.exception;

public class HystrixException extends Exception {
    public HystrixException(Throwable throwable) {
        super(throwable);
    }
}
