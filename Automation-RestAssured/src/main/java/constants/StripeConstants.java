package constants;

public final class StripeConstants {
    private StripeConstants() {}

    // endpoints
    public static final String CUSTOMERS       = "/customers";
    public static final String CHARGES         = "/charges";
    public static final String REFUNDS         = "/refunds";
    public static final String PAYMENT_METHODS = "/payment_methods";
    public static final String SUBSCRIPTIONS   = "/subscriptions";
    public static final String INVOICES        = "/invoices";
    public static final String PRODUCTS        = "/products";
    public static final String PRICES          = "/prices";
    public static final String PAYMENT_INTENTS = "/payment_intents";
    public static final String ACCOUNT         = "/account";

    // subscription/charge statuses
    public static final String STATUS_SUCCEEDED = "succeeded";
    public static final String STATUS_FAILED    = "failed";
    public static final String STATUS_ACTIVE    = "active";
    public static final String STATUS_CANCELED  = "canceled";
    public static final String STATUS_TRIALING  = "trialing";
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_REQUIERS_ACTIONS = "requires_action";
    public static final String STATUS_REQUIRES_PAYMENT_METHOD = "requires_payment_method";
    public static final String STATUS_REFUNDS = "refunds";
    public static final String STATUS_REFUND_SUCCEEDED = "refund_succeeded";
    public static final String STATUS_CARD_DECLINE = "card_declined";


    // Card Tokens — needed for Subscriptions
    public static final String CARD_SUCCESS = "tok_visa";
    public static final String CARD_DECLINED = "tok_visa_chargeDeclined";
    public static final String CARD_INSUFFICIENT = "tok_visa_chargeDeclinedInsufficientFunds";
    // Response Time
    public static final long MAX_RESPONSE_TIME_MS = 6000L;
    public static final String CARD_3DS = "tok_threeDSecure2Required";
    public static final String CARD_MASTERCARD = "tok_mastercard";
    public static final String CARD_DEBIT = "tok_visa_debit";

    //currencies
    public static final String USD = "usd";
    public static final String EUR = "eur";
    public static final String GBP = "gbp";
    // PaymentMethod Tokens
    public static final String PAYMENT_METHOD_DECLINED = "pm_card_visa_chargeDeclined";
    public static final String PAYMENT_METHOD_DEBIT = "pm_card_visa_debit";
    public static final String PAYMENT_METHOD_VISA = "pm_card_visa";
    public static final String PAYMENT_METHOD_MASTERCARD = "pm_card_mastercard";
    public static final String PAYMENT_METHOD_3DS  = "pm_card_threeDSecure2Required";
    public static final String CARD_VISA =  "tok_visa";

}
