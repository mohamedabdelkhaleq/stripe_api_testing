package builders;

import java.util.LinkedHashMap;
import java.util.Map;

public class SubscriptionRequestBuilder {

    private String customer;
    private String priceId;
    private String defaultPaymentMethod;
    private String trialEnd;
    private Boolean cancelAtPeriodEnd;
    private String subscriptionItemId;
    private String newPriceId;
    private final Map<String, String> metadata = new LinkedHashMap<>();

    public static SubscriptionRequestBuilder builder() { return new SubscriptionRequestBuilder(); }

    public SubscriptionRequestBuilder customer(String customerId) { this.customer = customerId; return this; }
    public SubscriptionRequestBuilder price(String priceId) { this.priceId = priceId; return this; }
    public SubscriptionRequestBuilder defaultPaymentMethod(String pmId) { this.defaultPaymentMethod = pmId; return this; }
    public SubscriptionRequestBuilder trialEnd(String timestamp) { this.trialEnd = timestamp; return this; }
    public SubscriptionRequestBuilder cancelAtPeriodEnd(Boolean cancel) { this.cancelAtPeriodEnd = cancel; return this; }
    public SubscriptionRequestBuilder metadata(String key, String value) { this.metadata.put("metadata[" + key + "]", value); return this; }

    // used when changing a subscription's price mid-cycle
    public SubscriptionRequestBuilder updateItem(String itemId, String newPriceId) {
        this.subscriptionItemId = itemId;
        this.newPriceId = newPriceId;
        return this;
    }

    public Map<String, String> build() {
        Map<String, String> form = new LinkedHashMap<>();
        if (customer != null)              form.put("customer", customer);
        if (priceId != null)               form.put("items[0][price]", priceId);
        if (defaultPaymentMethod != null)  form.put("default_payment_method", defaultPaymentMethod);
        if (trialEnd != null)              form.put("trial_end", trialEnd);
        if (cancelAtPeriodEnd != null)     form.put("cancel_at_period_end", String.valueOf(cancelAtPeriodEnd));
        if (subscriptionItemId != null)    form.put("items[0][id]", subscriptionItemId);
        if (newPriceId != null)            form.put("items[0][price]", newPriceId);
        form.putAll(metadata);
        return form;
    }
}
