package builders;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerRequestBuilder {

    private String email;
    private String name;
    private String phone;
    private String description;
    private final Map<String, String> metadata = new LinkedHashMap<>();

    public static CustomerRequestBuilder builder() {
        return new CustomerRequestBuilder();
    }

    public CustomerRequestBuilder email(String email) {
        this.email = email; return this;
    }
    public CustomerRequestBuilder name(String name) {
        this.name = name; return this;
    }
    public CustomerRequestBuilder phone(String phone) {
        this.phone = phone; return this;
    }
    public CustomerRequestBuilder description(String description) {
        this.description = description; return this;
    }

    // use metadata(key, value) for any field e.g. order_id, tier, last_purchase
    public CustomerRequestBuilder metadata(String key, String value) {
        this.metadata.put("metadata[" + key + "]", value); return this;
    }

    public Map<String, String> build() {
        Map<String, String> form = new LinkedHashMap<>();
        if (email       != null) form.put("email",       email);
        if (name        != null) form.put("name",        name);
        if (phone       != null) form.put("phone",       phone);
        if (description != null) form.put("description", description);
        form.putAll(metadata);
        return form;
    }
}
