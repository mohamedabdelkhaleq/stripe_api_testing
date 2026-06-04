package builders;

import Requestes.PaymentMethodRequest;


public class PaymentMethodBuilder {
    private String type;
    private String cardToken;
    private String customer;
    private String email;
    private String name;
    public static PaymentMethodBuilder builder() {
        return new PaymentMethodBuilder();
    }
    public PaymentMethodBuilder type(String type) {
        this.type = type;
        return this;
    }
    public PaymentMethodBuilder cardToken(String cardToken) {
        this.cardToken = cardToken;
        return this;
    }
    public PaymentMethodBuilder customer(String customer) {
        this.customer = customer;
        return this;
    }
    public PaymentMethodBuilder email(String email) {
        this.email = email;
        return this;
    }
    public PaymentMethodBuilder name(String name) {
        this.name = name;
        return this;
    }
    public PaymentMethodRequest build() {
        return new PaymentMethodRequest(type, cardToken, customer,email,name);
    }


}
