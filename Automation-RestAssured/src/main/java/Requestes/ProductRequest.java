package Requestes;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
public class ProductRequest {
    private final String name;
    private final String description;
    private final boolean active;
    private final Map<String, String> metadata;

    public ProductRequest(String name, String description, boolean active, Map<String, String> metadata) {
        this.name = name;
        this.description = description;
        this.active = active;
        this.metadata = (metadata != null) ? new HashMap<>(metadata) : new HashMap<>();
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public Map<String, String> getMetadata() { return metadata; }
    public Map<String, String> toFormParams() {
        Map<String, String> form = new LinkedHashMap<>();
        if (name             != null) form.put("name",               name);
        if (description      != null) form.put("description",        description);
        if (active) form.put("active",String.valueOf(active));
        if (metadata != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                form.put("metadata[" + entry.getKey() + "]", entry.getValue());
            }
        }
        return form;
    }
}
