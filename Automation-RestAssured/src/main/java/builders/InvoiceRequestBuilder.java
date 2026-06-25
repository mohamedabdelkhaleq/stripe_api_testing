package builders;

import java.util.LinkedHashMap;
import java.util.Map;

public class InvoiceRequestBuilder {
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String subscription;


    public static InvoiceRequestBuilder Builder() {return new InvoiceRequestBuilder();}
    public InvoiceRequestBuilder customerId(String customerId) {
        this.customerId = customerId;
        return this;
    }
    public InvoiceRequestBuilder customerName(String customerName) {
        this.customerName = customerName;
        return this;
    }
    public InvoiceRequestBuilder customerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
        return this;
    }
    public InvoiceRequestBuilder subscription(String subscription) {
        this.subscription = subscription;
        return this;
    }

    public Map<String, String> build() {
        Map<String, String> form = new LinkedHashMap<>();
        if (customerId != null) form.put("customer", customerId);
        if (customerName != null) form.put("customer_name", customerName);
        if (customerEmail != null) form.put("customer_email", customerEmail);
        if (subscription != null) form.put("subscription", subscription);
        return form;
    }
}
