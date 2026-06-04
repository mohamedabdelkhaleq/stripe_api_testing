package constants;

public final class StripeConstants {
    private StripeConstants() {
        // Private constructor to prevent instantiation
    }

    // Endpoints
    public static final String CUSTOMERS = "/customers";
    public static final String CHARGES = "/charges";
    public static final String REFUNDS = "/refunds";
    public static final String PAYMENT_METHODS = "/payment_methods";
    public static final String SUBSCRIPTIONS = "/subscriptions";
    public static final String INVOICES = "/invoices";
    public static final String PRODUCTS = "/products";
    public static final String PRICES = "/prices";
    public static final String PAYMENT_INTENTS = "/payment_intents";



//    Expected Statuses
    public static final String STATUS_SUCCEEDED = "succeeded";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_CANCELED = "canceled";
    public static final String STATUS_TRIALING = "trialing";
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_REQUIERS_ACTIONS = "requires_action";
    public static final String STATUS_REQUIRES_PAYMENT_METHOD = "requires_payment_method";
    public static final String STATUS_REFUNDS = "refunds";

    // Card Tokens — needed for Subscriptions
    public static final String CARD_SUCCESS = "tok_visa";
    public static final String CARD_DECLINED = "tok_visa_chargeDeclined";
    public static final String CARD_INSUFFICIENT = "tok_visa_chargeDeclinedInsufficientFunds";

    public static final String CARD_3DS = "tok_threeDSecure2Required";
    public static final String CARD_MASTERCARD = "tok_mastercard";
    public static final String CARD_DEBIT = "tok_visa_debit";
    // Response Time
    public static final long MAX_RESPONSE_TIME_MS = 3000L;
    //currencies
    public static final String USD = "usd";
    public static final String EUR = "eur";
    public static final String GBP = "gbp";
    // PaymentMethod Tokens
    public static final String PAYMENT_METHOD_DECLINED = "pm_card_visa_chargeDeclined";
    public static final String PAYMENT_METHOD_DEBIT = "pm_card_visa_debit";
    public static final String PAYMENT_METHOD_VISA = "pm_card_visa";
    public static final String PAYMENT_METHOD_MASTERCARD = "pm_card_visa_mastercard";
    public static final String PAYMENT_METHOD_3DS  = "pm_card_threeDSecure2Required";


}

