package builders;


import java.util.LinkedHashMap;
import java.util.Map;

public class PriceRequestBuilder {

    private String unitAmount;
    private String currency;
    private String productId;
    private String recurringInterval;
    private Boolean active;
    private String product_data;
    private final Map<String, String> metadata = new LinkedHashMap<>();

    public static PriceRequestBuilder builder() { return new PriceRequestBuilder(); }

    public PriceRequestBuilder unitAmount(int amount) { this.unitAmount = String.valueOf(amount); return this; }
    public PriceRequestBuilder currency(String currency) { this.currency = currency; return this; }
    public PriceRequestBuilder product(String productId) { this.productId = productId; return this; }
    public PriceRequestBuilder recurringInterval(String interval) { this.recurringInterval = interval; return this; }
    public PriceRequestBuilder active(Boolean active) { this.active = active; return this; }
    public PriceRequestBuilder metadata(String key, String value) { this.metadata.put("metadata[" + key + "]", value); return this; }
    public PriceRequestBuilder product_data(String name) { this.product_data = name; return this; }

    public Map<String, String> build() {
        Map<String, String> form = new LinkedHashMap<>();
        if (unitAmount != null)        form.put("unit_amount", unitAmount);
        if (currency != null)          form.put("currency", currency);
        if (productId != null)         form.put("product", productId);
        if (recurringInterval != null) form.put("recurring[interval]", recurringInterval);
        if (active != null)            form.put("active", String.valueOf(active));
        if (product_data != null) form.put("product_data[name]", product_data);
        form.putAll(metadata);
        return form;
    }
}
        form.putAll(metadata);
        return form;
    }
}
