package Requestes;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentMethodRequest {
    private String type;
    private String cardToken;
    private String customer;
    private String email;
    private String name;
    public PaymentMethodRequest(String type, String cardToken, String customer,String email, String name) {
        this.type = type;
        this.cardToken = cardToken;
        this.customer = customer;
        this.email =email;
        this.name = name;
    }
    public String getType() { return type; }
    public String getCardToken() { return cardToken; }
    public String getCustomer() { return customer; }

    public Map<String, String> toCreateParams() {
        Map<String, String> form = new LinkedHashMap<>();
        if (type      != null) form.put("type", type);
        if (cardToken != null) form.put("card[token]",cardToken);
        if (customer != null) form.put("customer", customer);
        if (email != null) form.put("email", email);
        if (name != null) form.put("name", name);
        return form;
    }
}
