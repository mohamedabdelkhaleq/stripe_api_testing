package builders;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProductRequestBuilder {

    private String name;
    private String description;
    private Boolean active;
    private final Map<String, String> metadata = new LinkedHashMap<>();

    public static ProductRequestBuilder builder() { return new ProductRequestBuilder(); }

    public ProductRequestBuilder name(String name) { this.name = name; return this; }
    public ProductRequestBuilder description(String description) { this.description = description; return this; }
    public ProductRequestBuilder active(Boolean active) { this.active = active; return this; }
    public ProductRequestBuilder metadata(String key, String value) { this.metadata.put("metadata[" + key + "]", value); return this; }

    public Map<String, String> build() {
        Map<String, String> form = new LinkedHashMap<>();
        if (name != null)        form.put("name", name);
        if (description != null) form.put("description", description);
        if (active != null)      form.put("active", String.valueOf(active));
        form.putAll(metadata);
        return form;
    }
}
