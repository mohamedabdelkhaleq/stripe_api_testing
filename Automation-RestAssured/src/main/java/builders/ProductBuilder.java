package builders;

import Requestes.ProductRequest;

import java.util.HashMap;
import java.util.Map;
public class ProductBuilder {
    private String name;
    private String description;
    private boolean active;
    private Map<String, String> metadata = new HashMap<>();
    public static ProductBuilder builder() {
        return new ProductBuilder();
    }
    public ProductBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ProductBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    public ProductBuilder metadata(String key, String value) {
        this.metadata.put(key, value);
        return this;
    }

    public ProductBuilder metadata(Map<String, String> metadata) {
        this.metadata = (metadata != null) ? new HashMap<>(metadata) : new HashMap<>();
        return this;
    }

    public ProductRequest build() {
        return new ProductRequest(name, description, active, metadata);
    }
}
