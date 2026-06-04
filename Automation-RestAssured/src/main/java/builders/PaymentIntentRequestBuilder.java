package builders;
import Requestes.PaymentIntentRequest;
public class PaymentIntentRequestBuilder {
    private String amount;
    private String currency;
    private String payment_Method;
    private String description;
    private boolean confirm;
    private String returnUrl;
    private String customer;

    public static PaymentIntentRequestBuilder Builder() {
        return new PaymentIntentRequestBuilder();
    }
    public PaymentIntentRequestBuilder amount(String amount) {
        this.amount = amount;
        return this;
    }
    public PaymentIntentRequestBuilder currency(String currency) {
        this.currency = currency;
        return this;
    }
    public PaymentIntentRequestBuilder payment_Method(String payment_Method) {
        this.payment_Method = payment_Method;
        return this;
    }
    public PaymentIntentRequestBuilder description(String description) {
        this.description = description;
        return this;
    }
    public PaymentIntentRequestBuilder confirm(boolean confirm) {
        this.confirm = confirm;
        return this;
    }
    public PaymentIntentRequestBuilder returnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
        return this;
    }
    public PaymentIntentRequestBuilder customer(String customer) {
        this.customer = customer;
        return this;
    }
    public PaymentIntentRequest  build() {
        return new PaymentIntentRequest(amount, currency,payment_Method,description, confirm, returnUrl, customer);
    }


}
