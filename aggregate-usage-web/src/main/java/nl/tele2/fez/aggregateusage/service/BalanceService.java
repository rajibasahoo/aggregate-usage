package nl.tele2.fez.aggregateusage.service;

import java.util.concurrent.Future;

public abstract class BalanceService<T> {
    protected static final String SENDER = "sender";
    protected static final String LEGAL_ENTITY = "legalEntity";
    protected static final String MSISDN = "MSISDN";
    protected static final String EFFECTIVE_DATE = "effectiveDate";
    protected static final String LIMITS_FLAG = "LimitsFlag";
    protected static final String INVOICE_LANGUAGE = "InvoiceLanguage";
    protected static final String ALLOCATIONS_FLAG = "AllocationsFlag";

    abstract Future<T> getBalance(String msisdn, String businessProcessId, String conversationId);
}
