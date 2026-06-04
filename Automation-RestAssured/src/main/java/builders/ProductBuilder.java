package builders;

import Requestes.ProductRequest;

import java.util.HashMap;
import java.util.Map;
public class ProductRequestBuilder {
    private String name;
    private String description;
    private boolean active;
    private Map<String, String> metadata = new HashMap<>();
    public static ProductRequestBuilder builder() {
        return new ProductRequestBuilder();
    }
    public ProductRequestBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProductRequestBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ProductRequestBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    public ProductRequestBuilder metadata(String key, String value) {
        this.metadata.put(key, value);
        return this;
    }

    public ProductRequestBuilder metadata(Map<String, String> metadata) {
        this.metadata = (metadata != null) ? new HashMap<>(metadata) : new HashMap<>();
        return this;
    }

    public ProductRequest build() {
        return new ProductRequest(name, description, active, metadata);
    }
}
