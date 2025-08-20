package org.kuraterut.paymentservice.logger;

import lombok.Data;

@Data
public class PaymentAccountLogs {
    private PaymentAccountLogs(){}
    public static final String FOUND = "{} Payment Account Found: {}";
    public static final String FOUND_LIST = "{} Payment Account List Found: {}";
    public static final String NOT_FOUND = "{} Payment Account Not Found with ID: {}";
    public static final String TRANSACTION_SAVED = "{} Transaction Saved Successfully: {}";
    public static final String NOT_FOUND_FORMAT = "%s Payment Account Not Found with ID: %d";
}
