package builders;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerRequestBuilder {
    private String email;
    private String name;
    private String phone;
    private String description;
    private String metadataOrderId;
    private String metadataPriority;

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
    public CustomerRequestBuilder metadataOrderId(String orderId) {
        this.metadataOrderId = orderId; return this;
    }
    public CustomerRequestBuilder metadataPriority(String priority) {
        this.metadataPriority = priority; return this;
    }

    public Map<String, String> build() {
        Map<String, String> form = new LinkedHashMap<>();
        if (email            != null) form.put("email",              email);
        if (name             != null) form.put("name",               name);
        if (phone            != null) form.put("phone",              phone);
        if (description      != null) form.put("description",        description);
        if (metadataOrderId  != null) form.put("metadata[order_id]", metadataOrderId);
        if (metadataPriority != null) form.put("metadata[priority]", metadataPriority);
        return form;
    }
}