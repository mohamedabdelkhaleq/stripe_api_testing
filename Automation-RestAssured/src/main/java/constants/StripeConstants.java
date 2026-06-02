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

    // test card tokens
    public static final String CARD_VISA     = "tok_visa";
    public static final String CARD_DECLINED = "tok_visa_chargeDeclined";

    public static final long MAX_RESPONSE_TIME_MS = 6000L;
}
